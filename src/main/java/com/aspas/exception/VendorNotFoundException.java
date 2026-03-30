package com.aspas.exception;

/**
 * ================================================================
 * VendorNotFoundException
 * ================================================================
 *
 * Thrown when a vendor lookup fails.
 *
 * UML Traceability:
 *   - Sequence Diagram: Message #19 "SC → V : getVendorAddress()"
 *     If no vendor exists for a part in D3 Vendor Directory
 *   - UC-04: Fetch Vendor Address
 *
 * ================================================================
 */
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