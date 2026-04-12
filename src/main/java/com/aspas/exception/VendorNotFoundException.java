package com.aspas.exception;

public class VendorNotFoundException extends RuntimeException {

    public VendorNotFoundException(Long vendorId) {
        super("Vendor not found with ID: " + vendorId);
    }

    public VendorNotFoundException(String vendorName) {
        super("Vendor not found with name: " + vendorName);
    }

    public static VendorNotFoundException forPart(String partNumber) {
        return new VendorNotFoundException(
            "No vendor found for part: " + partNumber
        );
    }

    private VendorNotFoundException(String message, boolean flag) {
        super(message);
    }
}