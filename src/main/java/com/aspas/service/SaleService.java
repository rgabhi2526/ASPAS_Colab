package com.aspas.service;

import com.aspas.config.BusinessDateBounds;
import com.aspas.exception.InsufficientStockException;
import com.aspas.exception.PartNotFoundException;
import com.aspas.model.document.SalesTransactionDoc;
import com.aspas.model.dto.SaleRequestDTO;
import com.aspas.model.dto.SaleResponseDTO;
import com.aspas.model.dto.SalesDayStatsDTO;
import com.aspas.model.entity.SparePart;
import com.aspas.repository.jpa.SparePartRepository;
import com.aspas.repository.mongo.SalesTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * ================================================================
 * SaleService — DFD Process 1.0: Process Sales & Update Inventory
 * ================================================================
 *
 * UML Traceability:
 *   - DFD Process: P1.0 Process Sales & Update Inventory
 *   - Sequence Diagram: Messages #1 through #8
 *   - Use Case: UC-01 Process Sale & Update Inventory
 *   - Class Diagram: SystemController.processSale()
 *
 * Complete message flow implemented:
 *   Msg #1 : Owner → SC  : processSale(partNo, qty)
 *   Msg #2 : SC → SP     : getPartDetails()
 *   Msg #3 : SP → SC     : unitPrice
 *   Msg #4 : SC → SP     : updateQuantity(-qty)
 *   Msg #5 : SP → SC     : void
 *   Msg #6 : SC → ST     : <<create>> (part, qty, unitPrice)
 *   Msg #7 : SC → ST     : logSale()
 *   Msg #8 : SC → Owner  : success
 *
 * Data Flow:
 *   - READS from D1 (Inventory — MySQL)
 *   - WRITES to D1 (Inventory — MySQL, update stock)
 *   - WRITES to D2 (Sales Log — MongoDB, log transaction)
 *
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final SparePartRepository sparePartRepository;
    private final SalesTransactionRepository salesTransactionRepository;
    private final BusinessDateBounds businessDateBounds;

    /**
     * Process a sale: deduct stock and log transaction.
     *
     * This method implements the COMPLETE sequence diagram flow
     * for Process 1.0 (Messages #1 through #8).
     *
     * @param request sale request containing partNumber and quantity
     * @return sale response with transaction details
     * @throws PartNotFoundException if part doesn't exist
     * @throws InsufficientStockException if not enough stock
     */
    @Transactional
    public SaleResponseDTO processSale(SaleRequestDTO request) {

        String partNumber = request.getPartNumber();
        int quantity = request.getQuantity();

        log.info("═══ PROCESS 1.0: Processing sale — Part: {}, Qty: {} ═══",
            partNumber, quantity);

        // ─────────────────────────────────────────────
        // MESSAGE #2-3: SC → SP : getPartDetails()
        // Read from D1 Inventory File (MySQL)
        // ─────────────────────────────────────────────
        SparePart part = sparePartRepository.findByPartNumber(partNumber)
            .orElseThrow(() -> {
                log.error("Part not found: {}", partNumber);
                return new PartNotFoundException(partNumber);
            });

        double unitPrice = part.getUnitPrice();
        log.debug("  Msg #2-3: Part found — {} [{}], Price: ₹{}, Stock: {}",
            part.getPartNumber(), part.getPartName(), unitPrice, part.getCurrentQuantity());

        // ─────────────────────────────────────────────
        // VALIDATION: Check sufficient stock
        // ─────────────────────────────────────────────
        if (part.getCurrentQuantity() < quantity) {
            log.error("  Insufficient stock: requested {}, available {}",
                quantity, part.getCurrentQuantity());
            throw new InsufficientStockException(
                partNumber, quantity, part.getCurrentQuantity()
            );
        }

        // ─────────────────────────────────────────────
        // MESSAGE #4-5: SC → SP : updateQuantity(-qty)
        // Write to D1 Inventory File (MySQL)
        // ─────────────────────────────────────────────
        part.updateQuantity(-quantity);
        sparePartRepository.save(part);
        log.debug("  Msg #4-5: Stock updated — New quantity: {}", part.getCurrentQuantity());

        // ─────────────────────────────────────────────
        // MESSAGE #6: SC → ST : <<create>>
        // Create new SalesTransaction document
        // ─────────────────────────────────────────────
        String transactionId = generateTransactionId();
        LocalDateTime now = LocalDateTime.now();

        SalesTransactionDoc transaction = new SalesTransactionDoc(
            transactionId,
            now,
            part.getPartNumber(),
            part.getPartName(),
            quantity,
            unitPrice
        );
        log.debug("  Msg #6: Transaction created — ID: {}, Revenue: ₹{}",
            transactionId, transaction.getRevenueAmount());

        // ─────────────────────────────────────────────
        // MESSAGE #7: SC → ST : logSale()
        // Write to D2 Sales Log (MongoDB)
        // ─────────────────────────────────────────────
        transaction.logSale();
        salesTransactionRepository.save(transaction);
        log.debug("  Msg #7: Transaction logged to MongoDB");

        // ─────────────────────────────────────────────
        // MESSAGE #8: SC → Owner : success
        // Build response DTO
        // ─────────────────────────────────────────────
        SaleResponseDTO response = SaleResponseDTO.builder()
            .transactionId(transactionId)
            .transactionDate(now)
            .partNumber(part.getPartNumber())
            .partName(part.getPartName())
            .quantitySold(quantity)
            .unitPrice(unitPrice)
            .revenueAmount(transaction.getRevenueAmount())
            .message("Sale processed successfully")
            .build();

        log.info("═══ PROCESS 1.0 COMPLETE: TXN {} — Revenue: ₹{} ═══",
            transactionId, transaction.getRevenueAmount());

        return response;
    }

    /**
     * List sales transactions for a calendar day (MongoDB D2).
     */
    public List<SalesTransactionDoc> listTransactionsForDate(LocalDate date) {
        Date start = businessDateBounds.startOfCalendarDay(date);
        Date end = businessDateBounds.endOfCalendarDay(date);
        return salesTransactionRepository.findByTransactionDateBetween(start, end);
    }

    /**
     * Most recent sales from MongoDB (newest first), for dashboard activity.
     */
    public List<SalesTransactionDoc> listRecentTransactions(int limit) {
        int n = Math.max(1, Math.min(limit, 50));
        return salesTransactionRepository.findAllByOrderByTransactionDateDesc(PageRequest.of(0, n));
    }

    /**
     * Dashboard KPIs: count and revenue sum from {@code sales_transactions} for one calendar day
     * in the configured business timezone (same bounds as listTransactionsForDate).
     */
    public SalesDayStatsDTO getSalesStatsForDay(LocalDate date) {
        Date start = businessDateBounds.startOfCalendarDay(date);
        Date end = businessDateBounds.endOfCalendarDay(date);
        List<SalesTransactionDoc> list = salesTransactionRepository.findByTransactionDateBetween(start, end);
        double rev = 0.0;
        int cnt = 0;
        if (list != null && !list.isEmpty()) {
            cnt = list.size();
            rev = list.stream().mapToDouble(SaleService::lineRevenue).sum();
        }
        return SalesDayStatsDTO.builder()
            .totalRevenue(rev)
            .transactionCount(cnt)
            .build();
    }

    private static double lineRevenue(SalesTransactionDoc t) {
        if (t == null) {
            return 0.0;
        }
        Double r = t.getRevenueAmount();
        return r != null ? r : 0.0;
    }

    /**
     * Generate a unique transaction ID.
     * Format: TXN-YYYYMMDD-UUID(8chars)
     *
     * @return unique transaction ID
     */
    private String generateTransactionId() {
        String datePart = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN-" + datePart + "-" + uuidPart;
    }
}