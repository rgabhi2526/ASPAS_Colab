package com.aspas.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponseDTO {

    private String reportId;
    private String reportType;  

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    private Integer month;
    private Integer year;

    private Double totalRevenue;
    private Integer transactionCount;
    private Double averageDailyRevenue;

    private List<DailyDataPointDTO> dailyDataPoints;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyDataPointDTO {
        private Integer day;
        private Double revenue;
    }
}