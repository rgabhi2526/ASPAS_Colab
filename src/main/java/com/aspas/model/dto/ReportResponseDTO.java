package com.aspas.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ================================================================
 * ReportResponseDTO
 * ================================================================
 * 
 * Generic report response structure.
 * Contains either daily or monthly aggregated data.
 * 
 * ================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponseDTO {

    private String reportId;
    private String reportType;  // "DAILY" or "MONTHLY"

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    private Integer month;
    private Integer year;

    private Double totalRevenue;
    private Integer transactionCount;
    private Double averageDailyRevenue;

    // Populated for daily reports (from cached DailyRevenueReportDoc)
    private List<TopSellingPartDTO> topSellingParts;

    // For monthly reports: daily data points for graphing
    private List<DailyDataPointDTO> dailyDataPoints;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;

    /**
     * Inner class for daily data points.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyDataPointDTO {
        private Integer day;
        private Double revenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopSellingPartDTO {
        private String partNumber;
        private Integer qtySold;
        private Double revenue;
    }
}