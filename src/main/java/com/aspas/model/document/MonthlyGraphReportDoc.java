package com.aspas.model.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * MonthlyGraphReportDoc - MongoDB Document
 * ================================================================
 * 
 * UML Traceability:
 *   - Class Diagram: MonthlyGraphReport (extends Report, implements Printable)
 *   - Database: monthly_graph_reports collection (MongoDB)
 *   - Sequence Diagram: Message #26 "<<create>> MonthlyGraphReportDoc"
 *   - Use Case: UC-08 View Monthly Sales Graph
 * 
 * Aggregated monthly report with:
 *   - Daily data points (revenue per day)
 *   - Monthly total and average
 *   - Data suitable for graphing (frontends will plot this)
 * 
 * Structure:
 *   "dailyDataPoints": [
 *     { "day": 1, "revenue": 18500.00 },
 *     { "day": 2, "revenue": 22100.00 },
 *     ...
 *   ]
 * 
 * ================================================================
 */
@Document(collection = "monthly_graph_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyGraphReportDoc {

    @Id
    private String id;  // MongoDB _id

    private String reportId;            // Business key: RPT-MONTH-202603
    
    private Integer targetMonth;        // 1-12
    private Integer targetYear;         // 2026
    
    private String reportType = "MONTHLY";

    // Array of daily data points for graphing
    private List<DailyDataPoint> dailyDataPoints = new ArrayList<>();

    private Double monthlyTotal = 0.0;           // Sum of all days
    private Double averageDailyRevenue = 0.0;   // monthlyTotal / days

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;          // When report was created

    /**
     * Constructor.
     */
    public MonthlyGraphReportDoc(Integer month, Integer year) {
        this.targetMonth = month;
        this.targetYear = year;
        this.reportId = String.format("RPT-MONTH-%04d%02d", year, month);
        this.reportType = "MONTHLY";
        this.generatedAt = LocalDateTime.now();
    }

    /**
     * Generate the report (calculate aggregates from sales data).
     * 
     * UML Traceability: Sequence Diagram → Message #28 "report.generate()"
     * 
     * This is called by ReportService after aggregating transactions.
     */
    public void generate() {
        // Service layer populates dailyDataPoints, then we calculate totals
        if (!dailyDataPoints.isEmpty()) {
            monthlyTotal = dailyDataPoints.stream()
                .mapToDouble(d -> d.getRevenue())
                .sum();
            
            averageDailyRevenue = monthlyTotal / dailyDataPoints.size();
        }
        
        this.generatedAt = LocalDateTime.now();
    }

    /**
     * Plot graph (backend returns data for frontend to visualize).
     * 
     * No actual graphing in backend; we return the data.
     * Frontend will use this to render a chart.
     */
    public void plotGraph() {
        // Data is already in dailyDataPoints[]
        // Frontend gets this JSON and renders the graph
        System.out.println("Monthly graph data ready for visualization:");
        System.out.println("Month: " + targetMonth + "/" + targetYear);
        System.out.println("Data points: " + dailyDataPoints.size());
    }

    /**
     * Get report summary.
     * @return summary string
     */
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

    /**
     * Embedded class for daily revenue data points.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyDataPoint {
        private Integer day;            // 1-31
        private Double revenue;         // ₹ for that day
    }
}