package com.aspas.model.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "daily_revenue_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRevenueReportDoc {

    @Id
    private String id;  

    private String reportId;            
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;       
    
    private String reportType = "DAILY";

    private Double dailyTotal = 0.0;    
    private Integer transactionCount = 0;  

    private List<TopSellingPart> topSellingParts = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;  

    public DailyRevenueReportDoc(String reportId, LocalDate reportDate) {
        this.reportId = reportId;
        this.reportDate = reportDate;
        this.reportType = "DAILY";
        this.generatedAt = LocalDateTime.now();
    }

    public void generate() {
        
        this.generatedAt = LocalDateTime.now();
    }

    public Double getDailyTotal() {
        return dailyTotal != null ? dailyTotal : 0.0;
    }

    public String getReportSummary() {
        return String.format(
            "Daily Report [%s] | Date: %s | Revenue: ₹%.2f | Transactions: %d",
            reportId, reportDate, getDailyTotal(), transactionCount
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopSellingPart {
        private String partNumber;
        private Integer qtySold;
        private Double revenue;
    }
}