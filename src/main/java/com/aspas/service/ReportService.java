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

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final SalesTransactionRepository salesTransactionRepository;
    private final DailyRevenueReportRepository dailyRevenueReportRepository;
    private final MonthlyGraphReportRepository monthlyGraphReportRepository;

    public ReportResponseDTO getDailyRevenueReport(LocalDate date) {

        log.info("═══ PROCESS 4.0: Generating Daily Revenue Report for {} ═══", date);

        Optional<DailyRevenueReportDoc> existing =
            dailyRevenueReportRepository.findByReportDate(date);

        if (existing.isPresent()) {
            log.info("  Returning cached report: {}", existing.get().getReportId());
            return buildDailyResponse(existing.get());
        }

        String reportId = "RPT-DAILY-" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        DailyRevenueReportDoc report = new DailyRevenueReportDoc(reportId, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

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

        report.generate();

        dailyRevenueReportRepository.save(report);

        log.info("═══ Daily Report Complete: {} — Revenue: ₹{}, Transactions: {} ═══",
            reportId, report.getDailyTotal(), report.getTransactionCount());

        return buildDailyResponse(report);
    }

    public ReportResponseDTO getMonthlyGraphReport(int year, int month) {

        log.info("═══ PROCESS 4.0: Generating Monthly Graph Report for {}/{} ═══",
            month, year);

        Optional<MonthlyGraphReportDoc> existing =
            monthlyGraphReportRepository.findByTargetMonthAndTargetYear(month, year);

        if (existing.isPresent()) {
            log.info("  Returning cached report: {}", existing.get().getReportId());
            return buildMonthlyResponse(existing.get());
        }

        MonthlyGraphReportDoc report = new MonthlyGraphReportDoc(month, year);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        List<DailyRevenueAggregate> dailyAggregates =
            salesTransactionRepository.aggregateDailyRevenueForMonth(
                startOfMonth, endOfMonth
            );

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

        report.generate();

        monthlyGraphReportRepository.save(report);

        log.info("═══ Monthly Report Complete: {} — Total: ₹{}, Avg: ₹{} ═══",
            report.getReportId(), report.getMonthlyTotal(), report.getAverageDailyRevenue());

        return buildMonthlyResponse(report);
    }

    public ReportResponseDTO regenerateDailyReport(LocalDate date) {
        log.info("Force regenerating daily report for: {}", date);

        dailyRevenueReportRepository.findByReportDate(date)
            .ifPresent(dailyRevenueReportRepository::delete);

        return getDailyRevenueReport(date);
    }

    public ReportResponseDTO regenerateMonthlyReport(int year, int month) {
        log.info("Force regenerating monthly report for: {}/{}", month, year);

        monthlyGraphReportRepository.findByTargetMonthAndTargetYear(month, year)
            .ifPresent(monthlyGraphReportRepository::delete);

        return getMonthlyGraphReport(year, month);
    }

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