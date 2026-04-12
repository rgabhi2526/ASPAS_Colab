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

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "3. Reports", description = "UC-05/07/08: Analytical Reports (Daily Revenue & Monthly Graph)")
public class ReportController {

    private final SystemControllerService systemController;
    private final ReportService reportService;

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

        ReportResponseDTO response = systemController.requestReport(
            "DAILY", date, null, null
        );

        return ResponseEntity.ok(response);
    }

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

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        ReportResponseDTO response = systemController.requestReport(
            "MONTHLY", null, year, month
        );

        return ResponseEntity.ok(response);
    }

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