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

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "2. Orders", description = "UC-03: Generate Daily End-of-Day Orders")
public class OrderController {

    private final SystemControllerService systemController;
    private final OrderService orderService;

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