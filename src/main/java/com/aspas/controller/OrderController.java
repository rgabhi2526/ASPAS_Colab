package com.aspas.controller;

import com.aspas.model.dto.OrderResponseDTO;
import com.aspas.service.OrderService;
import com.aspas.service.SystemControllerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * ================================================================
 * OrderController — REST API for Order Management
 * ================================================================
 *
 * UML Traceability:
 *   - Use Cases:
 *       UC-03: Generate Daily Orders (base)
 *       UC-02: Calculate JIT Thresholds (<<include>>)
 *       UC-04: Fetch Vendor Address (<<include>>)
 *       UC-06: Check Inventory vs Threshold (<<include>>)
 *   - DFD Process: P3.0 Generate Daily Orders
 *   - Sequence Diagram:
 *       Message #9  : Clock/Owner → SC : triggerEndOfDayOrder()
 *       Message #23 : SC → OL : print()
 *   - Actors: Shop Owner, System Clock
 *
 * Endpoints:
 *   POST /api/orders/generate          → Trigger order generation
 *   GET  /api/orders                   → List all orders
 *   GET  /api/orders/{id}              → Get specific order
 *   GET  /api/orders/{id}/print        → Get print-ready text
 *   GET  /api/orders/by-date/{date}    → Get order by date
 *
 * ================================================================
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "2. Orders", description = "UC-03: Generate Daily End-of-Day Orders")
public class OrderController {

    private final SystemControllerService systemController;
    private final OrderService orderService;

    /**
     * Trigger end-of-day order generation.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #9: "Clock/Owner → SC : triggerEndOfDayOrder()"
     *   DFD: Owner → P3.0 (End-of-Day Trigger)
     *
     * This triggers the COMPLETE end-of-day process:
     *   1. <<include>> JIT threshold recalculation (P2.0)
     *   2. Scan all parts against thresholds
     *   3. For below-threshold parts: fetch vendor, create order item
     *   4. Generate and print order list
     *
     * Normally triggered by the System Clock scheduler at 11:55 PM,
     * but can be manually triggered via this endpoint.
     *
     * @return generated order list with all items
     *
     * Example:
     *   POST /api/orders/generate
     */
    @PostMapping("/generate")
    @Operation(
        summary = "Generate end-of-day order",
        description = "Triggers complete EOD process: JIT calc → threshold check → " +
                      "vendor lookup → order generation → print. " +
                      "Maps to UC-03 (includes UC-02, UC-04, UC-06)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order generated successfully"),
        @ApiResponse(responseCode = "200", description = "Order already exists for today")
    })
    public ResponseEntity<OrderResponseDTO> generateDailyOrder() {
        log.info("API: POST /api/orders/generate — Manual EOD trigger");

        OrderResponseDTO response = systemController.triggerEndOfDayOrder();

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    /**
     * Get all order lists.
     *
     * @return list of all orders, newest first
     *
     * Example:
     *   GET /api/orders
     */
    @GetMapping
    @Operation(
        summary = "List all orders",
        description = "Returns all generated order lists sorted by date (newest first)"
    )
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        log.info("API: GET /api/orders");

        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Get a specific order by ID.
     *
     * @param orderId order database ID
     * @return the order with all items
     *
     * Example:
     *   GET /api/orders/1
     */
    @GetMapping("/{orderId}")
    @Operation(
        summary = "Get order by ID",
        description = "Returns a specific order list with all its order items"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long orderId
    ) {
        log.info("API: GET /api/orders/{}", orderId);

        OrderResponseDTO response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get an order by date.
     *
     * @param date order date (format: yyyy-MM-dd)
     * @return the order for that date
     *
     * Example:
     *   GET /api/orders/by-date/2026-03-15
     */
    @GetMapping("/by-date/{date}")
    @Operation(
        summary = "Get order by date",
        description = "Returns the order list generated on a specific date"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "No order for that date")
    })
    public ResponseEntity<OrderResponseDTO> getOrderByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("API: GET /api/orders/by-date/{}", date);

        OrderResponseDTO response = orderService.getOrderByDate(date);
        return ResponseEntity.ok(response);
    }

    /**
     * Get print-ready text output for an order.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #23: "SC → OL : print()"
     *   Interface: Printable.formatOutput()
     *   Output format: Plain text
     *
     * Returns the formatted order list as TEXT (not JSON).
     * Contains: Part# | Required Qty | Vendor Address
     *
     * @param orderId order ID
     * @return plain text order output
     *
     * Example:
     *   GET /api/orders/1/print
     */
    @GetMapping(value = "/{orderId}/print", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
        summary = "Get printable order text",
        description = "Returns print-ready formatted text output. " +
                      "Format: Part# | Qty | Vendor Address. " +
                      "Maps to Printable interface implementation."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Print text generated"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<String> getOrderPrintText(
            @PathVariable Long orderId
    ) {
        log.info("API: GET /api/orders/{}/print", orderId);

        String printText = orderService.getOrderPrintText(orderId);
        return ResponseEntity.ok(printText);
    }
}