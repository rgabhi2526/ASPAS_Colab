package com.aspas.controller;

import com.aspas.model.entity.SparePart;
import com.aspas.model.entity.Vendor;
import com.aspas.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ================================================================
 * VendorController — REST API for Vendor Management
 * ================================================================
 *
 * UML Traceability:
 *   - DFD Store: D3 Vendor Directory
 *   - Class Diagram: Vendor class
 *   - Use Case: UC-04 Fetch Vendor Address (<<include>> for UC-03)
 *   - Sequence Diagram: Message #19-20 "SC → V : getVendorAddress()"
 *
 * Endpoints:
 *   GET    /api/vendors                       → List all vendors
 *   GET    /api/vendors/{id}                  → Get vendor by ID
 *   POST   /api/vendors                       → Add new vendor
 *   PUT    /api/vendors/{id}                  → Update vendor
 *   DELETE /api/vendors/{id}                  → Remove vendor
 *   GET    /api/vendors/search?q=keyword      → Search vendors
 *   GET    /api/vendors/by-part/{partId}      → Vendors for a part
 *   GET    /api/vendors/{id}/parts            → Parts supplied by vendor
 *
 * ════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/vendors")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "5. Vendors", description = "UC-04: Vendor Directory Management (DFD Store D3)")
public class VendorController {

    private final VendorService vendorService;

    /**
     * Get all vendors.
     *
     * Example:
     *   GET /api/vendors
     */
    @GetMapping
    @Operation(summary = "List all vendors", description = "Returns complete vendor directory")
    public ResponseEntity<List<Vendor>> getAllVendors() {
        log.info("API: GET /api/vendors");
        return ResponseEntity.ok(vendorService.getAllVendors());
    }

    /**
     * Spare parts linked to a vendor through the part_vendor association.
     *
     * Example:
     *   GET /api/vendors/1/parts
     */
    @GetMapping("/{vendorId}/parts")
    @Operation(
        summary = "Parts supplied by vendor",
        description = "Returns spare parts mapped to this vendor in the part_vendor join table"
    )
    public ResponseEntity<List<SparePart>> getPartsForVendor(
            @PathVariable Long vendorId
    ) {
        log.info("API: GET /api/vendors/{}/parts", vendorId);
        return ResponseEntity.ok(vendorService.getPartsSuppliedByVendor(vendorId));
    }

    /**
     * Get a vendor by ID.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #19-20 "SC → V : getVendorAddress()"
     *   UC-04: Fetch Vendor Address
     *
     * Example:
     *   GET /api/vendors/1
     */
    @GetMapping("/{vendorId}")
    @Operation(
        summary = "Get vendor by ID",
        description = "Returns vendor details including address. Maps to UC-04."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vendor found"),
        @ApiResponse(responseCode = "404", description = "Vendor not found")
    })
    public ResponseEntity<Vendor> getVendorById(
            @PathVariable Long vendorId
    ) {
        log.info("API: GET /api/vendors/{}", vendorId);
        return ResponseEntity.ok(vendorService.getVendorById(vendorId));
    }

    /**
     * Add a new vendor.
     *
     * Example:
     *   POST /api/vendors
     *   {
     *     "vendorName": "New Auto Parts Ltd",
     *     "vendorAddress": "456 Industrial Zone, Mumbai, India",
     *     "contactNumber": "+91-22-12345678",
     *     "email": "orders@newautoparts.com"
     *   }
     */
    @PostMapping
    @Operation(summary = "Add new vendor", description = "Creates a new vendor in the directory")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Vendor created"),
        @ApiResponse(responseCode = "400", description = "Duplicate vendor name or validation error")
    })
    public ResponseEntity<Vendor> addVendor(
            @RequestBody Vendor vendor
    ) {
        log.info("API: POST /api/vendors — {}", vendor.getVendorName());
        Vendor saved = vendorService.addVendor(vendor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Update a vendor.
     *
     * UML Traceability:
     *   Class Diagram → Vendor.updateContactInfo()
     *
     * Example:
     *   PUT /api/vendors/1
     *   {
     *     "vendorName": "Bosch Auto Parts (Updated)",
     *     "vendorAddress": "New Address 789",
     *     "contactNumber": "+49-30-99999",
     *     "email": "newcontact@bosch.com"
     *   }
     */
    @PutMapping("/{vendorId}")
    @Operation(summary = "Update vendor", description = "Updates vendor details and contact info")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vendor updated"),
        @ApiResponse(responseCode = "404", description = "Vendor not found")
    })
    public ResponseEntity<Vendor> updateVendor(
            @PathVariable Long vendorId,
            @RequestBody Vendor updatedVendor
    ) {
        log.info("API: PUT /api/vendors/{}", vendorId);
        return ResponseEntity.ok(vendorService.updateVendor(vendorId, updatedVendor));
    }

    /**
     * Delete a vendor.
     *
     * Example:
     *   DELETE /api/vendors/1
     */
    @DeleteMapping("/{vendorId}")
    @Operation(summary = "Delete vendor", description = "Removes vendor from directory")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Vendor deleted"),
        @ApiResponse(responseCode = "404", description = "Vendor not found")
    })
    public ResponseEntity<Void> deleteVendor(
            @PathVariable Long vendorId
    ) {
        log.info("API: DELETE /api/vendors/{}", vendorId);
        vendorService.deleteVendor(vendorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search vendors by name keyword.
     *
     * Example:
     *   GET /api/vendors/search?q=bosch
     */
    @GetMapping("/search")
    @Operation(summary = "Search vendors", description = "Case-insensitive keyword search by name")
    public ResponseEntity<List<Vendor>> searchVendors(
            @RequestParam("q") String keyword
    ) {
        log.info("API: GET /api/vendors/search?q={}", keyword);
        return ResponseEntity.ok(vendorService.searchVendors(keyword));
    }

    /**
     * Get vendors that supply a specific part.
     *
     * UML Traceability:
     *   Class Diagram → SparePart *──1..* Vendor (supplied by)
     *   Uses the part_vendor many-to-many join table
     *
     * Example:
     *   GET /api/vendors/by-part/1
     */
    @GetMapping("/by-part/{partId}")
    @Operation(
        summary = "Vendors for a part",
        description = "Returns all vendors that supply a specific spare part. " +
                      "Queries the part_vendor join table (many-to-many)."
    )
    public ResponseEntity<List<Vendor>> getVendorsForPart(
            @PathVariable Long partId
    ) {
        log.info("API: GET /api/vendors/by-part/{}", partId);
        return ResponseEntity.ok(vendorService.getVendorsForPart(partId));
    }
}