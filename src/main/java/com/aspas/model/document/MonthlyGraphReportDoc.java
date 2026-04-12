package com.aspas.model.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "monthly_graph_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyGraphReportDoc {

    @Id
    private String id;  

    private String reportId;            
    
    private Integer targetMonth;        
    private Integer targetYear;         
    
    private String reportType = "MONTHLY";

    private List<DailyDataPoint> dailyDataPoints = new ArrayList<>();

    private Double monthlyTotal = 0.0;           
    private Double averageDailyRevenue = 0.0;   

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;          

    public MonthlyGraphReportDoc(Integer month, Integer year) {
        this.targetMonth = month;
        this.targetYear = year;
        this.reportId = String.format("RPT-MONTH-%04d%02d", year, month);
        this.reportType = "MONTHLY";
        this.generatedAt = LocalDateTime.now();
    }

    public void generate() {
        
        if (!dailyDataPoints.isEmpty()) {
            monthlyTotal = dailyDataPoints.stream()
                .mapToDouble(d -> d.getRevenue())
                .sum();
            
            averageDailyRevenue = monthlyTotal / dailyDataPoints.size();
        }
        
        this.generatedAt = LocalDateTime.now();
    }

    public void plotGraph() {

        System.out.println("Monthly graph data ready for visualization:");
        System.out.println("Month: " + targetMonth + "/" + targetYear);
        System.out.println("Data points: " + dailyDataPoints.size());
    }

    public String getReportSummary() {
        return String.format(
            "Monthly Report [%s] | %d/%d | Total: ₹%.2f | Avg Daily: ₹%.2f",
            reportId, targetMonth, targetYear, getMonthlyTotal(), getAverageDailyRevenue()
        );
    }

    public Double getMonthlyTotal() {
        return monthlyTotal != null ? monthlyTotal : 0.0;
    }

    public Double getAverageDailyRevenue() {
        return averageDailyRevenue != null ? averageDailyRevenue : 0.0;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyDataPoint {
        private Integer day;            
        private Double revenue;         
    }
}