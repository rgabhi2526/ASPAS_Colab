package com.aspas.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * ================================================================
 * ReportRequestDTO
 * ================================================================
 * 
 * Request parameters for generating reports.
 * Used via REST API: GET /api/reports/daily/{date}
 *                    GET /api/reports/monthly/{year}/{month}
 * 
 * UML Traceability:
 *   - Sequence Diagram: Message #24 "Owner → SC.requestReport(type)"
 *   - Use Cases: UC-07, UC-08
 * 
 * ================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDTO {

    private String reportType;  // "DAILY" or "MONTHLY"

    @Min(value = 1)
    @Max(value = 12)
    private Integer month;

    @Min(value = 2000)
    @Max(value = 2100)
    private Integer year;

    private String date;  // For daily reports: "2026-03-15"
}