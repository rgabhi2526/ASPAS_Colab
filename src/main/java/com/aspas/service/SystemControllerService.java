package com.aspas.service;

import com.aspas.model.dto.OrderResponseDTO;
import com.aspas.model.dto.ReportResponseDTO;
import com.aspas.model.dto.SaleRequestDTO;
import com.aspas.model.dto.SaleResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemControllerService {

    private final SaleService saleService;
    private final JITService jitService;
    private final OrderService orderService;
    private final ReportService reportService;

    public SaleResponseDTO processSale(SaleRequestDTO request) {
        log.info("╔══ SystemController: processSale({}, {}) ══╗",
            request.getPartNumber(), request.getQuantity());
        return saleService.processSale(request);
    }

    public int calculateJITThresholds() {
        log.info("╔══ SystemController: calculateJITThresholds() ══╗");
        return jitService.calculateJITThresholds();
    }

    public OrderResponseDTO triggerEndOfDayOrder() {
        log.info("╔══ SystemController: triggerEndOfDayOrder() ══╗");
        return orderService.generateDailyOrder();
    }

    public ReportResponseDTO requestReport(
            String type, LocalDate date, Integer year, Integer month
    ) {
        log.info("╔══ SystemController: requestReport(type={}) ══╗", type);

        if ("MONTHLY".equalsIgnoreCase(type)) {

            log.info("  ALT → Monthly Graph Report for {}/{}", month, year);
            return reportService.getMonthlyGraphReport(year, month);
        } else {

            LocalDate reportDate = (date != null) ? date : LocalDate.now();
            log.info("  ALT → Daily Revenue Report for {}", reportDate);
            return reportService.getDailyRevenueReport(reportDate);
        }
    }
}