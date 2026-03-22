package com.aspas.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ================================================================
 * GlobalExceptionHandler — Unified Error Response Handler
 * ================================================================
 *
 * Catches all exceptions thrown by controllers/services and
 * converts them into structured JSON error responses.
 *
 * Ensures consistent error format across all API endpoints:
 * {
 *   "timestamp": "2026-03-15T14:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Spare part not found with part number: SP-XXX-999",
 *   "path": "/api/sales"
 * }
 *
 * ================================================================
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle PartNotFoundException.
     *
     * UML: Thrown when Seq Msg #2 findByPartNumber() fails.
     *
     * @return 404 Not Found
     */
    @ExceptionHandler(PartNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePartNotFound(
            PartNotFoundException ex
    ) {
        log.error("Part not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handle InsufficientStockException.
     *
     * UML: Thrown when Seq Msg #4 updateQuantity() fails validation.
     *
     * @return 400 Bad Request
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(
            InsufficientStockException ex
    ) {
        log.error("Insufficient stock: {}", ex.getMessage());

        Map<String, Object> body = buildErrorBody(HttpStatus.BAD_REQUEST, ex.getMessage());
        body.put("partNumber", ex.getPartNumber());
        body.put("requestedQuantity", ex.getRequestedQty());
        body.put("availableQuantity", ex.getAvailableQty());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handle VendorNotFoundException.
     *
     * UML: Thrown when Seq Msg #19 getVendorAddress() fails.
     *
     * @return 404 Not Found
     */
    @ExceptionHandler(VendorNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleVendorNotFound(
            VendorNotFoundException ex
    ) {
        log.error("Vendor not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handle OrderNotFoundException.
     *
     * @return 404 Not Found
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(
            OrderNotFoundException ex
    ) {
        log.error("Order not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handle validation errors (@Valid annotation failures).
     *
     * @return 400 Bad Request with field-level errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {
        log.error("Validation failed: {}", ex.getMessage());

        Map<String, Object> body = buildErrorBody(
            HttpStatus.BAD_REQUEST, "Validation failed"
        );

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handle IllegalArgumentException (business rule violations).
     *
     * @return 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        log.error("Invalid argument: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handle all other unexpected exceptions.
     *
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex
    ) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred: " + ex.getMessage()
        );
    }

    // ══════════════════════════════════════════
    //  HELPER METHODS
    // ══════════════════════════════════════════

    /**
     * Build a standard error response.
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String message
    ) {
        return ResponseEntity
            .status(status)
            .body(buildErrorBody(status, message));
    }

    /**
     * Build the error response body map.
     */
    private Map<String, Object> buildErrorBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}