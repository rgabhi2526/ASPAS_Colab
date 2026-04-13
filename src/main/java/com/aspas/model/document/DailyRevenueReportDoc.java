package com.aspas.model.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * DailyRevenueReportDoc - MongoDB Document
 * ================================================================
 * 
 * UML Traceability:
 *   - Class Diagram: DailyRevenueReport (extends Report, implements Printable)
 *   - Database: daily_revenue_reports collection (MongoDB)
 *   - Sequence Diagram: Message #25 "<<create>> DailyRevenueReportDoc"
 *   - Use Case: UC-07 View Daily Revenue Log
 * 
 * Aggregated daily report showing:
 *   - Total revenue for the day
 *   - Transaction count
 *   - Top selling parts
 * 
 * Generated each day (can be manual or scheduled).
 * 
 * ================================================================
 */
@Document(collection = "daily_revenue_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRevenueReportDoc {

    @Id
    private String id;  // MongoDB _id

    private String reportId;            // Business key: RPT-DAILY-20260315
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;       // Date being reported on
    
    private String reportType = "DAILY";

    private Double dailyTotal = 0.0;    // Total revenue for the day
    private Integer transactionCount = 0;  // Number of sales

    // Embedded list of top-selling parts
    private List<TopSellingPart> topSellingParts = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;  // When report was created

    /**
     * Constructor.
     */
    public DailyRevenueReportDoc(String reportId, LocalDate reportDate) {
        this.reportId = reportId;
        this.reportDate = reportDate;
        this.reportType = "DAILY";
        this.generatedAt = LocalDateTime.now();
    }

    /**
     * Generate the report (calculate aggregates from sales data).
     * 
     * UML Traceability: Sequence Diagram → Message #28 "report.generate()"
     * 
     * This is called by ReportService after fetching transactions.
     */
    public void generate() {
        // Service layer will populate data, then call this
        this.generatedAt = LocalDateTime.now();
    }

    /**
     * Get daily total revenue.
     * @return total
     */
    public Double getDailyTotal() {
        return dailyTotal != null ? dailyTotal : 0.0;
    }

    /**
     * Get report summary.
     * @return summary string
     */
    public String getReportSummary() {
        return String.format(
            "Daily Report [%s] | Date: %s | Revenue: ₹%.2f | Transactions: %d",
            reportId, reportDate, getDailyTotal(), transactionCount
        );
    }

    /**
     * Embedded class for top-selling parts.
     */
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