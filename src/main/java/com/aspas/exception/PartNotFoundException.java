package com.aspas.exception;

public class PartNotFoundException extends RuntimeException {

    private final String partNumber;

    public PartNotFoundException(String partNumber) {
        super("Spare part not found with part number: " + partNumber);
        this.partNumber = partNumber;
    }

    public PartNotFoundException(Long partId) {
        super("Spare part not found with ID: " + partId);
        this.partNumber = String.valueOf(partId);
    }

    public String getPartNumber() {
        return partNumber;
    }
}