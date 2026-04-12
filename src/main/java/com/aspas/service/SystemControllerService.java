package com.aspas.service;

import com.aspas.model.dto.OrderResponseDTO;
import com.aspas.model.dto.SalesDayStatsDTO;
import com.aspas.model.dto.ReportResponseDTO;
import com.aspas.model.dto.SaleRequestDTO;
import com.aspas.model.dto.SaleResponseDTO;
import com.aspas.model.document.SalesTransactionDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * ================================================================
 * SystemControllerService — Central Orchestrator
 * ================================================================
 *
 * UML Traceability:
 *   - Class Diagram: SystemController class
 *   - Maps to ALL 4 DFD Processes:
 *       P1.0 → processSale()              → delegates to SaleService
 *       P2.0 → calculateJITThresholds()   → delegates to JITService
 *       P3.0 → triggerEndOfDayOrder()      → delegates to OrderService
 *       P4.0 → requestReport()             → delegates to ReportService
 *
 *   - Sequence Diagram: This is the ":SystemController" participant
 *       All messages from the Shop Owner and System Clock
 *       are routed through this orchestrator.
 *
 *   - Note from Class Diagram:
 *       "Central Orchestrator — Maps to all 4 DFD Processes"
 *
 * This service does NOT contain business logic itself.
 * It DELEGATES to specialized services, matching the
 * sequence diagram's message routing pattern.
 *
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemControllerService {

    private final SaleService saleService;
    private final JITService jitService;
    private final OrderService orderService;
    private final ReportService reportService;

    // ══════════════════════════════════════════
    //  P1.0: PROCESS SALE
    // ══════════════════════════════════════════

    /**
     * Process a sale.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #1: "Owner → SC : processSale(partNo, qty)"
     *   Class Diagram → SystemController.processSale()
     *   DFD → P1.0
     *
     * Delegates to SaleService which handles:
     *   - Stock deduction (D1)
     *   - Transaction logging (D2)
     *
     * @param request sale request
     * @return sale result
     */
    public SaleResponseDTO processSale(SaleRequestDTO request) {
        log.info("╔══ SystemController: processSale({}, {}) ══╗",
            request.getPartNumber(), request.getQuantity());
        return saleService.processSale(request);
    }

    public List<SalesTransactionDoc> listTransactionsForDate(LocalDate date) {
        return saleService.listTransactionsForDate(date);
    }

    public SalesDayStatsDTO getSalesStatsForDay(LocalDate date) {
        return saleService.getSalesStatsForDay(date);
    }

    // ══════════════════════════════════════════
    //  P2.0: CALCULATE JIT THRESHOLDS
    // ══════════════════════════════════════════

    /**
     * Calculate JIT thresholds for all parts.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #10: "SC → SC : calculateJITThresholds()"
     *   Class Diagram → SystemController.calculateJITThresholds()
     *   DFD → P2.0
     *
     * Can be triggered:
     *   1. As part of end-of-day order (<<include>>)
     *   2. Manually via API
     *   3. By System Clock scheduler
     *
     * @return number of thresholds updated
     */
    public int calculateJITThresholds() {
        log.info("╔══ SystemController: calculateJITThresholds() ══╗");
        return jitService.calculateJITThresholds();
    }

    // ══════════════════════════════════════════
    //  P3.0: GENERATE DAILY ORDER
    // ══════════════════════════════════════════

    /**
     * Trigger end-of-day order generation.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #9: "Clock → SC : triggerEndOfDayOrder()"
     *   Class Diagram → SystemController.triggerEndOfDayOrder()
     *   DFD → P3.0
     *
     * This includes:
     *   1. <<include>> P2.0 JIT calculation
     *   2. Inventory scan against thresholds
     *   3. Vendor address lookup
     *   4. Order list generation + print
     *
     * @return generated order
     */
    public List<OrderResponseDTO> triggerEndOfDayOrder() {
        log.info("╔══ SystemController: triggerEndOfDayOrder() ══╗");
        List<OrderResponseDTO> orders = orderService.generateDailyOrder();
        return orders != null ? orders : Collections.emptyList();
    }

    // ══════════════════════════════════════════
    //  P4.0: GENERATE REPORTS
    // ══════════════════════════════════════════

    /**
     * Request an analytical report.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #24: "Owner → SC : requestReport(type)"
     *   Class Diagram → SystemController.requestReport()
     *   DFD → P4.0
     *
     * ALT fragment:
     *   - type == "DAILY"   → UC-07 View Daily Revenue Log
     *   - type == "MONTHLY" → UC-08 View Monthly Sales Graph
     *
     * @param type report type ("DAILY" or "MONTHLY")
     * @param date for daily reports
     * @param year for monthly reports
     * @param month for monthly reports
     * @return report data
     */
    public ReportResponseDTO requestReport(
            String type, LocalDate date, Integer year, Integer month
    ) {
        log.info("╔══ SystemController: requestReport(type={}) ══╗", type);

        // ─────────────────────────────────────────────
        // SEQUENCE DIAGRAM: ALT fragment
        // ─────────────────────────────────────────────
        if ("MONTHLY".equalsIgnoreCase(type)) {
            // SEQUENCE DIAGRAM: Message #26
            // <<extend>> UC-08: View Monthly Sales Graph
            log.info("  ALT → Monthly Graph Report for {}/{}", month, year);
            return reportService.getMonthlyGraphReport(year, month);
        } else {
            // SEQUENCE DIAGRAM: Message #27
            // <<extend>> UC-07: View Daily Revenue Log
            LocalDate reportDate = (date != null) ? date : LocalDate.now();
            log.info("  ALT → Daily Revenue Report for {}", reportDate);
            return reportService.getDailyRevenueReport(reportDate);
        }
    }
}