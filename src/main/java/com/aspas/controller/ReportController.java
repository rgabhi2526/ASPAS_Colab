package com.aspas.controller;

import com.aspas.model.dto.ReportResponseDTO;
import com.aspas.service.ReportService;
import com.aspas.service.SystemControllerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * ================================================================
 * ReportController — REST API for Analytical Reports
 * ================================================================
 *
 * UML Traceability:
 *   - Use Cases:
 *       UC-05: Generate Analytical Reports (base)
 *       UC-07: View Daily Revenue Log (<<extend>>)
 *       UC-08: View Monthly Sales Graph (<<extend>>)
 *   - DFD Process: P4.0 Generate Analytical Reports
 *   - Sequence Diagram:
 *       Message #24 : Owner → SC : requestReport(type)
 *       Message #25 : ALT [type]
 *       Message #26 : <<create>> MonthlyGraphReport (if monthly)
 *       Message #27 : <<create>> DailyRevenueReport (if daily)
 *       Message #30 : SC → Owner : display report
 *   - Actor: Shop Owner
 *
 * Endpoints:
 *   GET /api/reports/daily/{date}              → Daily revenue report
 *   GET /api/reports/daily/{date}/refresh      → Force regenerate daily
 *   GET /api/reports/monthly/{year}/{month}    → Monthly graph report
 *   GET /api/reports/monthly/{year}/{month}/refresh → Force regenerate monthly
 *
 * ================================================================
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "3. Reports", description = "UC-05/07/08: Analytical Reports (Daily Revenue & Monthly Graph)")
public class ReportController {

    private final SystemControllerService systemController;
    private final ReportService reportService;

    /**
     * Get daily revenue report.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #24: "Owner → SC : requestReport('DAILY')"
     *   Sequence Diagram → Message #27: "<<create>> DailyRevenueReport"
     *   Use Case: UC-07 View Daily Revenue Log (<<extend>> from UC-05)
     *   DFD: Owner → P4.0 → reads D2 → Owner
     *
     * Returns:
     *   - Total revenue for the day
     *   - Transaction count
     *   - Top selling parts
     *
     * @param date report date (format: yyyy-MM-dd)
     * @return daily revenue report
     *
     * Example:
     *   GET /api/reports/daily/2026-03-15
     */
    @GetMapping("/daily/{date}")
    @Operation(
        summary = "Get daily revenue report",
        description = "Returns total revenue, transaction count, and top-selling parts for a specific day. " +
                      "Maps to UC-07 View Daily Revenue Log (<<extend>>). " +
                      "Data is read from MongoDB sales_transactions collection (DFD D2)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Report generated/retrieved"),
        @ApiResponse(responseCode = "400", description = "Invalid date format")
    })
    public ResponseEntity<ReportResponseDTO> getDailyReport(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("API: GET /api/reports/daily/{}", date);

        // Maps to Sequence Diagram ALT → Daily branch
        ReportResponseDTO response = systemController.requestReport(
            "DAILY", date, null, null
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Force regenerate daily report (ignore cache).
     *
     * @param date report date
     * @return fresh daily report
     *
     * Example:
     *   GET /api/reports/daily/2026-03-15/refresh
     */
    @GetMapping("/daily/{date}/refresh")
    @Operation(
        summary = "Regenerate daily report",
        description = "Deletes cached report and regenerates from raw sales data"
    )
    public ResponseEntity<ReportResponseDTO> refreshDailyReport(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("API: GET /api/reports/daily/{}/refresh", date);

        ReportResponseDTO response = reportService.regenerateDailyReport(date);
        return ResponseEntity.ok(response);
    }

    /**
     * Get monthly graph report.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #24: "Owner → SC : requestReport('MONTHLY')"
     *   Sequence Diagram → Message #26: "<<create>> MonthlyGraphReport"
     *   Use Case: UC-08 View Monthly Sales Graph (<<extend>> from UC-05)
     *   DFD: Owner → P4.0 → reads D2 → Owner
     *
     * Returns:
     *   - Daily data points array (day, revenue) for graph plotting
     *   - Monthly total revenue
     *   - Average daily revenue
     *
     * @param year report year (e.g., 2026)
     * @param month report month (1-12)
     * @return monthly graph report with daily data points
     *
     * Example:
     *   GET /api/reports/monthly/2026/3
     *
     * Response includes dailyDataPoints for frontend graphing:
     *   "dailyDataPoints": [
     *     { "day": 1, "revenue": 18500.00 },
     *     { "day": 2, "revenue": 22100.00 },
     *     ...
     *   ]
     */
    @GetMapping("/monthly/{year}/{month}")
    @Operation(
        summary = "Get monthly graph report",
        description = "Returns daily revenue data points for a month, suitable for graph rendering. " +
                      "Maps to UC-08 View Monthly Sales Graph (<<extend>>). " +
                      "Data aggregated from MongoDB sales_transactions (DFD D2)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Report generated/retrieved"),
        @ApiResponse(responseCode = "400", description = "Invalid month/year")
    })
    public ResponseEntity<ReportResponseDTO> getMonthlyReport(
            @PathVariable Integer year,
            @PathVariable Integer month
    ) {
        log.info("API: GET /api/reports/monthly/{}/{}", year, month);

        // Validate month range
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        // Maps to Sequence Diagram ALT → Monthly branch
        ReportResponseDTO response = systemController.requestReport(
            "MONTHLY", null, year, month
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Force regenerate monthly report (ignore cache).
     *
     * @param year report year
     * @param month report month
     * @return fresh monthly report
     *
     * Example:
     *   GET /api/reports/monthly/2026/3/refresh
     */
    @GetMapping("/monthly/{year}/{month}/refresh")
    @Operation(
        summary = "Regenerate monthly report",
        description = "Deletes cached report and regenerates from raw sales data"
    )
    public ResponseEntity<ReportResponseDTO> refreshMonthlyReport(
            @PathVariable Integer year,
            @PathVariable Integer month
    ) {
        log.info("API: GET /api/reports/monthly/{}/{}/refresh", year, month);

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        ReportResponseDTO response = reportService.regenerateMonthlyReport(year, month);
        return ResponseEntity.ok(response);
    }
}