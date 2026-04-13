package com.aspas.controller;

import com.aspas.model.dto.SaleRequestDTO;
import com.aspas.model.dto.SaleResponseDTO;
import com.aspas.service.SystemControllerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aspas.model.document.SalesTransactionDoc;
import com.aspas.model.dto.SalesDayStatsDTO;
import java.time.LocalDate;
import java.util.List;

/**
 * ================================================================
 * SaleController — REST API for Sales Processing
 * ================================================================
 *
 * UML Traceability:
 *   - Use Case     : UC-01 Process Sale & Update Inventory
 *   - DFD Process  : P1.0 Process Sales & Update Inventory
 *   - Sequence Diagram:
 *       Message #1 : Owner → SC : processSale(partNo, qty)
 *       Message #8 : SC → Owner : success
 *   - Actor        : Shop Owner
 *
 * Endpoint:
 *   POST /api/sales
 *
 * Flow:
 *   Owner sends sale request → Controller → SystemControllerService
 *   → SaleService → (MySQL + MongoDB) → Response
 *
 * ================================================================
 */
@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "1. Sales", description = "UC-01: Process Sale & Update Inventory")
public class SaleController {

    private final SystemControllerService systemController;

    /**
     * List transactions for a given day (defaults to today). UC-07 / D2 sales log.
     *
     * Example: GET /api/sales/transactions?date=2026-04-12
     */
    @GetMapping("/transactions")
    @Operation(
        summary = "List transactions for a day",
        description = "Returns sales from MongoDB sales_transactions for the given date (default: today)"
    )
    public ResponseEntity<List<SalesTransactionDoc>> listTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate d = date != null ? date : LocalDate.now();
        log.info("API: GET /api/sales/transactions?date={}", d);
        return ResponseEntity.ok(systemController.listTransactionsForDate(d));
    }

    /**
     * Latest sales from MongoDB (default 5), for dashboard recent activity.
     */
    @GetMapping("/transactions/recent")
    @Operation(
        summary = "Recent sales transactions",
        description = "Returns the newest documents from sales_transactions (default limit 5)"
    )
    public ResponseEntity<List<SalesTransactionDoc>> recentTransactions(
            @RequestParam(required = false, defaultValue = "5") int limit
    ) {
        log.info("API: GET /api/sales/transactions/recent?limit={}", limit);
        return ResponseEntity.ok(systemController.listRecentTransactions(limit));
    }

    /**
     * Aggregated transaction count and revenue for a day (MongoDB {@code sales_transactions}).
     */
    @GetMapping("/stats")
    @Operation(
        summary = "Daily sales stats from MongoDB",
        description = "totalRevenue and transactionCount aggregated from sales_transactions for the given date"
    )
    public ResponseEntity<SalesDayStatsDTO> salesStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate d = date != null ? date : LocalDate.now();
        log.info("API: GET /api/sales/stats?date={}", d);
        return ResponseEntity.ok(systemController.getSalesStatsForDay(d));
    }

    /**
     * Process a sale transaction.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #1: "Owner → SC : processSale(partNo, qty)"
     *   DFD: Owner → P1.0 (Sales Entry)
     *
     * What happens internally:
     *   1. Looks up part in MySQL (D1 Inventory)
     *   2. Validates sufficient stock
     *   3. Deducts quantity from inventory (MySQL)
     *   4. Creates and logs SalesTransaction (MongoDB D2)
     *   5. Returns transaction confirmation
     *
     * @param request contains partNumber and quantity
     * @return 201 Created with transaction details
     *
     * Example request:
     *   POST /api/sales
     *   {
     *     "partNumber": "SP-BRK-001",
     *     "quantity": 4
     *   }
     */
    @PostMapping
    @Operation(
        summary = "Process a sale",
        description = "Deducts stock from inventory and logs the transaction. " +
                      "Maps to UC-01 and DFD Process 1.0"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sale processed successfully"),
        @ApiResponse(responseCode = "404", description = "Part not found"),
        @ApiResponse(responseCode = "400", description = "Insufficient stock or invalid input")
    })
    public ResponseEntity<SaleResponseDTO> processSale(
            @Valid @RequestBody SaleRequestDTO request
    ) {
        log.info("API: POST /api/sales — Part: {}, Qty: {}",
            request.getPartNumber(), request.getQuantity());

        SaleResponseDTO response = systemController.processSale(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }
}