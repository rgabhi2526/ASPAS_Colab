package com.aspas.exception;

/**
 * ================================================================
 * InsufficientStockException
 * ================================================================
 *
 * Thrown when a sale request exceeds available stock.
 *
 * UML Traceability:
 *   - Sequence Diagram: Message #4 "SC → SP : updateQuantity(-qty)"
 *     If requested quantity > currentQuantity
 *   - UC-01: Process Sale — validation before deducting
 *
 * ================================================================
 */
public class InsufficientStockException extends RuntimeException {

    private final String partNumber;
    private final int requestedQty;
    private final int availableQty;

    public InsufficientStockException(String partNumber, int requested, int available) {
        super(String.format(
            "Insufficient stock for part %s: requested %d, available %d",
            partNumber, requested, available
        ));
        this.partNumber = partNumber;
        this.requestedQty = requested;
        this.availableQty = available;
    }

    public String getPartNumber() { return partNumber; }
    public int getRequestedQty() { return requestedQty; }
    public int getAvailableQty() { return availableQty; }
}