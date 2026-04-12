package com.aspas.exception;

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