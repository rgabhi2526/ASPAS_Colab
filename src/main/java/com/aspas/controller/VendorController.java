package com.aspas.controller;

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

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "5. Vendors", description = "UC-04: Vendor Directory Management (DFD Store D3)")
public class VendorController {

    private final VendorService vendorService;

    @GetMapping
    @Operation(summary = "List all vendors", description = "Returns complete vendor directory")
    public ResponseEntity<List<Vendor>> getAllVendors() {
        log.info("API: GET /api/vendors");
        return ResponseEntity.ok(vendorService.getAllVendors());
    }

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

    @GetMapping("/search")
    @Operation(summary = "Search vendors", description = "Case-insensitive keyword search by name")
    public ResponseEntity<List<Vendor>> searchVendors(
            @RequestParam("q") String keyword
    ) {
        log.info("API: GET /api/vendors/search?q={}", keyword);
        return ResponseEntity.ok(vendorService.searchVendors(keyword));
    }

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