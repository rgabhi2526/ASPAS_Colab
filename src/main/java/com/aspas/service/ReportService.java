package com.aspas.service;

import com.aspas.model.document.DailyRevenueReportDoc;
import com.aspas.model.document.DailyRevenueReportDoc.TopSellingPart;
import com.aspas.model.document.MonthlyGraphReportDoc;
import com.aspas.model.document.MonthlyGraphReportDoc.DailyDataPoint;
import com.aspas.model.dto.ReportResponseDTO;
import com.aspas.model.dto.ReportResponseDTO.DailyDataPointDTO;
import com.aspas.repository.mongo.DailyRevenueReportRepository;
import com.aspas.repository.mongo.MonthlyGraphReportRepository;
import com.aspas.repository.mongo.SalesTransactionRepository;
import com.aspas.repository.mongo.SalesTransactionRepository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ================================================================
 * ReportService — DFD Process 4.0: Generate Analytical Reports
 * ================================================================
 *
 * UML Traceability:
 *   - DFD Process: P4.0 Generate Analytical Reports
 *   - Sequence Diagram: Messages #24 through #30
 *   - Use Cases:
 *       UC-05: Generate Analytical Reports (base)
 *       UC-07: View Daily Revenue Log (<<extend>>)
 *       UC-08: View Monthly Sales Graph (<<extend>>)
 *   - Class Diagram:
 *       Report (abstract) → DailyRevenueReport, MonthlyGraphReport
 *
 * Sequence Diagram flow:
 *   Msg #24 : Owner → SC : requestReport(type)
 *   Msg #25 : ALT [type == "Monthly"]
 *     Msg #26 : SC → R : <<create>> MonthlyGraphReport()
 *   Msg #25 : ALT [type == "Daily"]
 *     Msg #27 : SC → R : <<create>> DailyRevenueReport()
 *   Msg #28 : R → ST : reads data
 *   Msg #29 : R → R  : generate()
 *   Msg #30 : SC → Owner : display report
 *
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final SalesTransactionRepository salesTransactionRepository;
    private final DailyRevenueReportRepository dailyRevenueReportRepository;
    private final MonthlyGraphReportRepository monthlyGraphReportRepository;

    // ══════════════════════════════════════════
    //  UC-07: VIEW DAILY REVENUE LOG (<<extend>>)
    // ══════════════════════════════════════════

    /**
     * Generate or retrieve a daily revenue report.
     *
     * UML Traceability:
     *   Sequence Diagram → Messages #27, #28, #29
     *   Use Case: UC-07 View Daily Revenue Log
     *   DFD: P4.0 reads from D2 Sales Log
     *
     * @param date the date to report on
     * @return report response DTO
     */
    public ReportResponseDTO getDailyRevenueReport(LocalDate date) {

        log.info("═══ PROCESS 4.0: Generating Daily Revenue Report for {} ═══", date);

        // Check if report already exists
        Optional<DailyRevenueReportDoc> existing =
            dailyRevenueReportRepository.findByReportDate(date);

        if (existing.isPresent()) {
            log.info("  Returning cached report: {}", existing.get().getReportId());
            return buildDailyResponse(existing.get());
        }

        // ─────────────────────────────────────────────
        // SEQUENCE DIAGRAM: Message #27
        // SC → R : <<create>> DailyRevenueReport()
        // ─────────────────────────────────────────────
        String reportId = "RPT-DAILY-" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        DailyRevenueReportDoc report = new DailyRevenueReportDoc(reportId, date);

        // ─────────────────────────────────────────────
        // SEQUENCE DIAGRAM: Message #28
        // R → ST : reads data (from MongoDB D2 Sales Log)
        // ─────────────────────────────────────────────
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Get daily total revenue
        List<DailyTotalAggregate> dailyTotals =
            salesTransactionRepository.aggregateDailyTotal(startOfDay, endOfDay);

        if (dailyTotals != null && !dailyTotals.isEmpty()) {
            DailyTotalAggregate total = dailyTotals.get(0);
            report.setDailyTotal(
                total.getTotalRevenue() != null ? total.getTotalRevenue() : 0.0
            );
            report.setTransactionCount(
                total.getTotalTransactions() != null ? total.getTotalTransactions() : 0
            );
        }

        // Get top selling parts for the day
        List<TopSellingAggregate> topParts =
            salesTransactionRepository.aggregateTopSellingParts(startOfDay, endOfDay);

        if (topParts != null) {
            List<TopSellingPart> topSellingParts = topParts.stream()
                .map(tp -> TopSellingPart.builder()
                    .partNumber(tp.getId())
                    .qtySold(tp.getTotalQty())
                    .revenue(tp.getTotalRevenue())
                    .build()
                )
                .collect(Collectors.toList());
            report.setTopSellingParts(topSellingParts);
        }

        // ─────────────────────────────────────────────
        // SEQUENCE DIAGRAM: Message #29
        // R → R : generate()
        // ─────────────────────────────────────────────
        report.generate();

        // Save report to MongoDB
        dailyRevenueReportRepository.save(report);

        log.info("═══ Daily Report Complete: {} — Revenue: ₹{}, Transactions: {} ═══",
            reportId, report.getDailyTotal(), report.getTransactionCount());

        return buildDailyResponse(report);
    }

    // ══════════════════════════════════════════
    //  UC-08: VIEW MONTHLY SALES GRAPH (<<extend>>)
    // ══════════════════════════════════════════

    /**
     * Generate or retrieve a monthly graph report.
     *
     * UML Traceability:
     *   Sequence Diagram → Messages #26, #28, #29
     *   Use Case: UC-08 View Monthly Sales Graph
     *   DFD: P4.0 reads from D2 Sales Log
     *
     * @param year report year
     * @param month report month (1-12)
     * @return report response DTO with daily data points for graphing
     */
    public ReportResponseDTO getMonthlyGraphReport(int year, int month) {

        log.info("═══ PROCESS 4.0: Generating Monthly Graph Report for {}/{} ═══",
            month, year);

        // Check if report already exists
        Optional<MonthlyGraphReportDoc> existing =
            monthlyGraphReportRepository.findByTargetMonthAndTargetYear(month, year);

        if (existing.isPresent()) {
            log.info("  Returning cached report: {}", existing.get().getReportId());
            return buildMonthlyResponse(existing.get());
        }

        // ─────────────────────────────────────────────
        // SEQUENCE DIAGRAM: Message #26
        // SC → R : <<create>> MonthlyGraphReport()
        // ─────────────────────────────────────────────
        MonthlyGraphReportDoc report = new MonthlyGraphReportDoc(month, year);

        // ─────────────────────────────────────────────
        // SEQUENCE DIAGRAM: Message #28
        // R → ST : reads data (from MongoDB D2 Sales Log)
        // ─────────────────────────────────────────────
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // Aggregate daily revenues for the month
        List<DailyRevenueAggregate> dailyAggregates =
            salesTransactionRepository.aggregateDailyRevenueForMonth(
                startOfMonth, endOfMonth
            );

        // Convert aggregates to data points
        List<DailyDataPoint> dataPoints = new ArrayList<>();

        if (dailyAggregates != null) {
            for (DailyRevenueAggregate agg : dailyAggregates) {
                dataPoints.add(DailyDataPoint.builder()
                    .day(agg.getId())
                    .revenue(agg.getDailyRevenue() != null ? agg.getDailyRevenue() : 0.0)
                    .build()
                );
            }
        }

        report.setDailyDataPoints(dataPoints);

        // ─────────────────────────────────────────────
        // SEQUENCE DIAGRAM: Message #29
        // R → R : generate() — calculates totals and averages
        // ─────────────────────────────────────────────
        report.generate();

        // Save report to MongoDB
        monthlyGraphReportRepository.save(report);

        log.info("═══ Monthly Report Complete: {} — Total: ₹{}, Avg: ₹{} ═══",
            report.getReportId(), report.getMonthlyTotal(), report.getAverageDailyRevenue());

        return buildMonthlyResponse(report);
    }

    /**
     * Regenerate a daily report (force refresh, ignoring cache).
     *
     * @param date the date to report on
     * @return fresh report
     */
    public ReportResponseDTO regenerateDailyReport(LocalDate date) {
        log.info("Force regenerating daily report for: {}", date);

        // Delete existing report if present
        dailyRevenueReportRepository.findByReportDate(date)
            .ifPresent(dailyRevenueReportRepository::delete);

        return getDailyRevenueReport(date);
    }

    /**
     * Regenerate a monthly report (force refresh, ignoring cache).
     *
     * @param year year
     * @param month month
     * @return fresh report
     */
    public ReportResponseDTO regenerateMonthlyReport(int year, int month) {
        log.info("Force regenerating monthly report for: {}/{}", month, year);

        monthlyGraphReportRepository.findByTargetMonthAndTargetYear(month, year)
            .ifPresent(monthlyGraphReportRepository::delete);

        return getMonthlyGraphReport(year, month);
    }

    // ══════════════════════════════════════════
    //  RESPONSE BUILDERS
    // ══════════════════════════════════════════

    /**
     * Build a ReportResponseDTO from a DailyRevenueReportDoc.
     */
    private ReportResponseDTO buildDailyResponse(DailyRevenueReportDoc doc) {
        return ReportResponseDTO.builder()
            .reportId(doc.getReportId())
            .reportType("DAILY")
            .reportDate(doc.getReportDate())
            .totalRevenue(doc.getDailyTotal())
            .transactionCount(doc.getTransactionCount())
            .generatedAt(doc.getGeneratedAt())
            .build();
    }

    /**
     * Build a ReportResponseDTO from a MonthlyGraphReportDoc.
     */
    private ReportResponseDTO buildMonthlyResponse(MonthlyGraphReportDoc doc) {

        List<DailyDataPointDTO> dataPointDTOs = doc.getDailyDataPoints()
            .stream()
            .map(dp -> DailyDataPointDTO.builder()
                .day(dp.getDay())
                .revenue(dp.getRevenue())
                .build()
            )
            .collect(Collectors.toList());

        return ReportResponseDTO.builder()
            .reportId(doc.getReportId())
            .reportType("MONTHLY")
            .month(doc.getTargetMonth())
            .year(doc.getTargetYear())
            .totalRevenue(doc.getMonthlyTotal())
            .averageDailyRevenue(doc.getAverageDailyRevenue())
            .dailyDataPoints(dataPointDTOs)
            .generatedAt(doc.getGeneratedAt())
            .build();
    }
}