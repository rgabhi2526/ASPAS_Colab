package com.aspas.service;

import com.aspas.config.BusinessDateBounds;
import com.aspas.model.document.DailyRevenueReportDoc;
import com.aspas.model.document.DailyRevenueReportDoc.TopSellingPart;
import com.aspas.model.document.MonthlyGraphReportDoc;
import com.aspas.model.document.MonthlyGraphReportDoc.DailyDataPoint;
import com.aspas.model.document.SalesTransactionDoc;
import com.aspas.model.dto.ReportResponseDTO;
import com.aspas.model.dto.ReportResponseDTO.DailyDataPointDTO;
import com.aspas.model.dto.ReportResponseDTO.HourlyDataPointDTO;
import com.aspas.model.dto.ReportResponseDTO.TopSellingPartDTO;
import com.aspas.repository.mongo.DailyRevenueReportRepository;
import com.aspas.repository.mongo.MonthlyGraphReportRepository;
import com.aspas.repository.mongo.SalesTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final BusinessDateBounds businessDateBounds;

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

        log.info("═══ PROCESS 4.0: Daily Revenue Report for {} (MongoDB daily_revenue_reports) ═══", date);

        Optional<DailyRevenueReportDoc> existing =
            dailyRevenueReportRepository.findTopByReportDateOrderByGeneratedAtDesc(date);

        if (existing.isPresent()) {
            log.info("  Loaded persisted report: {}", existing.get().getReportId());
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
        Date startOfDay = businessDateBounds.startOfCalendarDay(date);
        Date endOfDay = businessDateBounds.endOfCalendarDay(date);

        List<SalesTransactionDoc> txs =
            salesTransactionRepository.findByTransactionDateBetween(startOfDay, endOfDay);
        if (txs == null) {
            txs = List.of();
        }
        report.setDailyTotal(txs.stream().mapToDouble(ReportService::lineRevenue).sum());
        report.setTransactionCount(txs.size());

        // Compute top sellers from already-fetched transactions.
        // This avoids a Spring Data Mongo aggregation projection edge case observed on some setups.
        report.setTopSellingParts(computeTopSellingPartsFromTransactions(txs, 10));

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

        log.info("═══ PROCESS 4.0: Monthly Graph Report for {}/{} (MongoDB monthly_graph_reports) ═══",
            month, year);

        Optional<MonthlyGraphReportDoc> existing =
            monthlyGraphReportRepository.findTopByTargetMonthAndTargetYearOrderByGeneratedAtDesc(month, year);

        if (existing.isPresent()) {
            log.info("  Loaded persisted report: {}", existing.get().getReportId());
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
        Date startOfMonth = businessDateBounds.startOfCalendarDay(yearMonth.atDay(1));
        Date endOfMonth = businessDateBounds.endOfCalendarDay(yearMonth.atEndOfMonth());

        List<SalesTransactionDoc> txs =
            salesTransactionRepository.findByTransactionDateBetween(startOfMonth, endOfMonth);
        List<DailyDataPoint> dataPoints = computeDailyDataPointsFromTransactions(txs);

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

        // Delete all duplicates for the day before regenerating.
        dailyRevenueReportRepository.deleteByReportDate(date);

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

        monthlyGraphReportRepository.deleteByTargetMonthAndTargetYear(month, year);

        return getMonthlyGraphReport(year, month);
    }

    // ══════════════════════════════════════════
    //  RESPONSE BUILDERS
    // ══════════════════════════════════════════

    /**
     * Build a ReportResponseDTO from a DailyRevenueReportDoc.
     * KPIs, top sellers, and hourly buckets are derived from live {@code sales_transactions}
     * for the report date so the UI stays aligned with MongoDB D2 even if the cached report is stale.
     */
    private ReportResponseDTO buildDailyResponse(DailyRevenueReportDoc doc) {
        LocalDate date = doc.getReportDate();
        Date s = businessDateBounds.startOfCalendarDay(date);
        Date e = businessDateBounds.endOfCalendarDay(date);
        List<SalesTransactionDoc> txs = salesTransactionRepository.findByTransactionDateBetween(s, e);
        if (txs == null) {
            txs = List.of();
        }

        double totalRev = txs.stream().mapToDouble(ReportService::lineRevenue).sum();
        int txCount = txs.size();

        List<TopSellingPartDTO> tops = computeTopSellingPartDTOsFromTransactions(txs, 10);

        List<HourlyDataPointDTO> hourly = buildHourlyFromTransactions(txs);

        double avgTx = txCount > 0 ? totalRev / txCount : 0.0;

        return ReportResponseDTO.builder()
            .reportId(doc.getReportId())
            .reportType("DAILY")
            .reportDate(doc.getReportDate())
            .totalRevenue(totalRev)
            .transactionCount(txCount)
            .averageTransactionValue(avgTx)
            .topSellingParts(tops)
            .hourlyDataPoints(hourly)
            .generatedAt(doc.getGeneratedAt())
            .build();
    }

    /**
     * Build a ReportResponseDTO from a MonthlyGraphReportDoc.
     */
    private ReportResponseDTO buildMonthlyResponse(MonthlyGraphReportDoc doc) {

        List<DailyDataPointDTO> dataPointDTOs = new ArrayList<>();
        if (doc.getDailyDataPoints() != null) {
            dataPointDTOs = doc.getDailyDataPoints().stream()
                .filter(Objects::nonNull)
                .map(dp -> DailyDataPointDTO.builder()
                    .day(dp.getDay())
                    .revenue(dp.getRevenue() != null ? dp.getRevenue() : 0.0)
                    .build()
                )
                .collect(Collectors.toList());
        }

        double sumPoints = dataPointDTOs.stream()
            .mapToDouble(d -> d.getRevenue() != null ? d.getRevenue() : 0.0)
            .sum();

        Double monthlyTotal = doc.getMonthlyTotal();
        if (monthlyTotal == null || monthlyTotal == 0.0) {
            monthlyTotal = sumPoints > 0 ? sumPoints : 0.0;
        }

        Double averageDaily = doc.getAverageDailyRevenue();
        int n = dataPointDTOs.size();
        if ((averageDaily == null || averageDaily == 0.0) && n > 0) {
            averageDaily = sumPoints / n;
        }
        if (averageDaily == null) {
            averageDaily = 0.0;
        }

        return ReportResponseDTO.builder()
            .reportId(doc.getReportId())
            .reportType("MONTHLY")
            .month(doc.getTargetMonth())
            .year(doc.getTargetYear())
            .totalRevenue(monthlyTotal)
            .averageDailyRevenue(averageDaily)
            .dailyDataPoints(dataPointDTOs)
            .generatedAt(doc.getGeneratedAt())
            .build();
    }

    private List<HourlyDataPointDTO> buildHourlyFromTransactions(List<SalesTransactionDoc> txs) {
        double[] byHour = new double[24];
        if (txs != null) {
            for (SalesTransactionDoc t : txs) {
                if (t == null || t.getTransactionDate() == null) {
                    continue;
                }
                ZonedDateTime z = businessDateBounds.toZoned(t.getTransactionDate());
                byHour[z.getHour()] += lineRevenue(t);
            }
        }
        List<HourlyDataPointDTO> out = new ArrayList<>();
        for (int h = 8; h <= 20; h++) {
            out.add(HourlyDataPointDTO.builder().hour(h).revenue(byHour[h]).build());
        }
        return out;
    }

    private static double lineRevenue(SalesTransactionDoc t) {
        if (t == null) {
            return 0.0;
        }
        Double r = t.getRevenueAmount();
        return r != null ? r : 0.0;
    }

    private List<TopSellingPart> computeTopSellingPartsFromTransactions(
            List<SalesTransactionDoc> txs,
            int limit
    ) {
        if (txs == null || txs.isEmpty()) {
            return List.of();
        }

        Map<String, TopPartAccumulator> acc = new HashMap<>();
        for (SalesTransactionDoc t : txs) {
            if (t == null || t.getPartNumber() == null) {
                continue;
            }
            String partNumber = t.getPartNumber();
            TopPartAccumulator bucket = acc.computeIfAbsent(partNumber, k -> new TopPartAccumulator());
            bucket.partNumber = partNumber;
            bucket.qty += t.getQuantitySold() != null ? t.getQuantitySold() : 0;
            bucket.revenue += lineRevenue(t);
        }

        return acc.values().stream()
            .sorted(Comparator.comparingInt((TopPartAccumulator a) -> a.qty).reversed())
            .limit(Math.max(0, limit))
            .map(a -> TopSellingPart.builder()
                .partNumber(a.partNumber)
                .qtySold(a.qty)
                .revenue(a.revenue)
                .build())
            .collect(Collectors.toList());
    }

    private List<TopSellingPartDTO> computeTopSellingPartDTOsFromTransactions(
            List<SalesTransactionDoc> txs,
            int limit
    ) {
        return computeTopSellingPartsFromTransactions(txs, limit).stream()
            .map(p -> TopSellingPartDTO.builder()
                .partNumber(p.getPartNumber())
                .qtySold(p.getQtySold())
                .revenue(p.getRevenue())
                .build())
            .collect(Collectors.toList());
    }

    private static final class TopPartAccumulator {
        private String partNumber;
        private int qty;
        private double revenue;
    }

    private List<DailyDataPoint> computeDailyDataPointsFromTransactions(List<SalesTransactionDoc> txs) {
        if (txs == null || txs.isEmpty()) {
            return List.of();
        }

        Map<Integer, Double> byDay = new HashMap<>();
        for (SalesTransactionDoc t : txs) {
            if (t == null || t.getTransactionDate() == null) {
                continue;
            }
            ZonedDateTime zdt = businessDateBounds.toZoned(t.getTransactionDate());
            if (zdt == null) {
                continue;
            }
            int day = zdt.getDayOfMonth();
            byDay.merge(day, lineRevenue(t), Double::sum);
        }

        return byDay.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> DailyDataPoint.builder()
                .day(e.getKey())
                .revenue(e.getValue())
                .build())
            .collect(Collectors.toList());
    }
}