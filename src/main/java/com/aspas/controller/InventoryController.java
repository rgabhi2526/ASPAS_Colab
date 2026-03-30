package com.aspas.controller;

import com.aspas.model.entity.SparePart;
import com.aspas.model.entity.StorageRack;
import com.aspas.service.InventoryService;
import com.aspas.service.JITService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * ================================================================
 * InventoryController — REST API for Inventory Management
 * ================================================================
 *
 * UML Traceability:
 *   - DFD Store: D1 Inventory File
 *   - Class Diagram: SparePart, StorageRack entities
 *   - Use Case: UC-06 Check Inventory vs Threshold (supporting)
 *   - Use Case: UC-02 Calculate JIT Thresholds (manual trigger)
 *
 * Endpoints:
 *   PARTS:
 *     GET    /api/parts                   → List all parts
 *     GET    /api/parts/{partNumber}      → Get one part
 *     POST   /api/parts                   → Add new part
 *     PUT    /api/parts/{partNumber}      → Update part
 *     DELETE /api/parts/{partNumber}      → Remove part
 *     GET    /api/parts/search?q=keyword  → Search parts
 *     GET    /api/parts/below-threshold   → Parts needing reorder
 *     GET    /api/parts/by-rack/{rackNo}  → Parts in a rack
 *
 *   JIT:
 *     POST   /api/jit/calculate           → Manual JIT recalculation
 *     GET    /api/jit/thresholds          → View all thresholds
 *
 *   RACKS:
 *     GET    /api/racks                   → List all racks
 *     POST   /api/racks                   → Add new rack
 *     PUT    /api/parts/{partNo}/rack/{rackNo} → Assign part to rack
 *
 * ================================================================
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "4. Inventory", description = "Inventory & JIT Threshold Management (DFD Store D1)")
public class InventoryController {

    private final InventoryService inventoryService;
    private final JITService jitService;

    // ══════════════════════════════════════════
    //  SPARE PARTS CRUD
    // ══════════════════════════════════════════

    /**
     * Get all spare parts.
     *
     * Example:
     *   GET /api/parts
     */
    @GetMapping("/parts")
    @Operation(summary = "List all spare parts", description = "Returns complete inventory")
    public ResponseEntity<List<SparePart>> getAllParts() {
        log.info("API: GET /api/parts");
        return ResponseEntity.ok(inventoryService.getAllParts());
    }

    /**
     * Get a spare part by part number.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #2-3 "SC → SP : getPartDetails()"
     *
     * Example:
     *   GET /api/parts/SP-BRK-001
     */
    @GetMapping("/parts/{partNumber}")
    @Operation(
        summary = "Get part by number",
        description = "Returns a specific spare part. Maps to Seq Msg #2-3."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Part found"),
        @ApiResponse(responseCode = "404", description = "Part not found")
    })
    public ResponseEntity<SparePart> getPartByNumber(
            @PathVariable String partNumber
    ) {
        log.info("API: GET /api/parts/{}", partNumber);
        return ResponseEntity.ok(inventoryService.getPartByNumber(partNumber));
    }

    /**
     * Add a new spare part.
     *
     * Example:
     *   POST /api/parts
     *   {
     *     "partNumber": "SP-NEW-001",
     *     "partName": "New Part",
     *     "currentQuantity": 20,
     *     "thresholdValue": 10,
     *     "unitPrice": 500.00,
     *     "sizeCategory": "MEDIUM"
     *   }
     */
    @PostMapping("/parts")
    @Operation(summary = "Add new spare part", description = "Creates a new part in inventory")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Part created"),
        @ApiResponse(responseCode = "400", description = "Validation error or duplicate part number")
    })
    public ResponseEntity<SparePart> addPart(
            @Valid @RequestBody SparePart part
    ) {
        log.info("API: POST /api/parts — {}", part.getPartNumber());
        SparePart saved = inventoryService.addPart(part);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Update an existing spare part.
     *
     * Example:
     *   PUT /api/parts/SP-BRK-001
     *   {
     *     "partName": "Updated Brake Pad",
     *     "unitPrice": 475.00,
     *     "sizeCategory": "SMALL"
     *   }
     */
    @PutMapping("/parts/{partNumber}")
    @Operation(summary = "Update spare part", description = "Updates part details")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Part updated"),
        @ApiResponse(responseCode = "404", description = "Part not found")
    })
    public ResponseEntity<SparePart> updatePart(
            @PathVariable String partNumber,
            @RequestBody SparePart updatedPart
    ) {
        log.info("API: PUT /api/parts/{}", partNumber);
        return ResponseEntity.ok(inventoryService.updatePart(partNumber, updatedPart));
    }

    /**
     * Delete a spare part.
     *
     * Example:
     *   DELETE /api/parts/SP-BRK-001
     */
    @DeleteMapping("/parts/{partNumber}")
    @Operation(summary = "Delete spare part", description = "Removes part from inventory")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Part deleted"),
        @ApiResponse(responseCode = "404", description = "Part not found")
    })
    public ResponseEntity<Void> deletePart(
            @PathVariable String partNumber
    ) {
        log.info("API: DELETE /api/parts/{}", partNumber);
        inventoryService.deletePart(partNumber);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search parts by name keyword.
     *
     * Example:
     *   GET /api/parts/search?q=brake
     */
    @GetMapping("/parts/search")
    @Operation(summary = "Search parts by name", description = "Case-insensitive keyword search")
    public ResponseEntity<List<SparePart>> searchParts(
            @RequestParam("q") String keyword
    ) {
        log.info("API: GET /api/parts/search?q={}", keyword);
        return ResponseEntity.ok(inventoryService.searchParts(keyword));
    }

    /**
     * Get all parts below JIT threshold (needing reorder).
     *
     * UML Traceability:
     *   Use Case: UC-06 Check Inventory vs Threshold
     *   Sequence Diagram → Message #17 "SP.checkThreshold()"
     *
     * Example:
     *   GET /api/parts/below-threshold
     */
    @GetMapping("/parts/below-threshold")
    @Operation(
        summary = "Parts below JIT threshold",
        description = "Returns all parts where currentQuantity < thresholdValue. " +
                      "Maps to UC-06 Check Inventory vs Threshold."
    )
    public ResponseEntity<List<SparePart>> getPartsBelowThreshold() {
        log.info("API: GET /api/parts/below-threshold");
        return ResponseEntity.ok(inventoryService.getPartsBelowThreshold());
    }

    /**
     * Get parts stored in a specific rack.
     *
     * Example:
     *   GET /api/parts/by-rack/1
     */
    @GetMapping("/parts/by-rack/{rackNumber}")
    @Operation(summary = "Parts by rack", description = "Returns parts in a specific wall rack")
    public ResponseEntity<List<SparePart>> getPartsByRack(
            @PathVariable Integer rackNumber
    ) {
        log.info("API: GET /api/parts/by-rack/{}", rackNumber);
        return ResponseEntity.ok(inventoryService.getPartsByRack(rackNumber));
    }

    // ══════════════════════════════════════════
    //  JIT THRESHOLD MANAGEMENT
    // ══════════════════════════════════════════

    /**
     * Manually trigger JIT threshold recalculation.
     *
     * UML Traceability:
     *   Use Case: UC-02 Calculate JIT Thresholds
     *   DFD Process: P2.0
     *   Activity Diagram: JIT Logic Execution (complete)
     *   Sequence Diagram → Message #10-14
     *
     * Normally triggered automatically as part of end-of-day order,
     * but can be manually triggered via this endpoint.
     *
     * Example:
     *   POST /api/jit/calculate
     */
    @PostMapping("/jit/calculate")
    @Operation(
        summary = "Recalculate JIT thresholds",
        description = "Manually triggers JIT recalculation for all parts. " +
                      "Reads 7-day sales from MongoDB, updates thresholds in MySQL. " +
                      "Maps to UC-02 and DFD P2.0."
    )
    public ResponseEntity<Map<String, Object>> calculateJITThresholds() {
        log.info("API: POST /api/jit/calculate — Manual JIT trigger");

        int updatedCount = jitService.calculateJITThresholds();

        return ResponseEntity.ok(Map.of(
            "message", "JIT thresholds recalculated successfully",
            "partsUpdated", updatedCount,
            "lookbackDays", 7,
            "policy", "Threshold = total sales in last 7 days (1-week buffer)"
        ));
    }

    /**
     * View all current JIT thresholds.
     *
     * Example:
     *   GET /api/jit/thresholds
     */
    @GetMapping("/jit/thresholds")
    @Operation(
        summary = "View all JIT thresholds",
        description = "Returns all parts with their current threshold values"
    )
    public ResponseEntity<List<SparePart>> getAllThresholds() {
        log.info("API: GET /api/jit/thresholds");
        return ResponseEntity.ok(jitService.getAllThresholds());
    }

    // ══════════════════════════════════════════
    //  STORAGE RACKS
    // ══════════════════════════════════════════

    /**
     * Get all storage racks.
     *
     * Example:
     *   GET /api/racks
     */
    @GetMapping("/racks")
    @Operation(summary = "List all racks", description = "Returns all wall-mounted storage racks")
    public ResponseEntity<List<StorageRack>> getAllRacks() {
        log.info("API: GET /api/racks");
        return ResponseEntity.ok(inventoryService.getAllRacks());
    }

    /**
     * Add a new storage rack.
     *
     * Example:
     *   POST /api/racks
     *   {
     *     "rackNumber": 6,
     *     "wallLocation": "West Wall - Section A",
     *     "maxCapacity": 80
     *   }
     */
    @PostMapping("/racks")
    @Operation(summary = "Add new rack", description = "Creates a new wall-mounted storage rack")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Rack created"),
        @ApiResponse(responseCode = "400", description = "Duplicate rack number")
    })
    public ResponseEntity<StorageRack> addRack(
            @RequestBody StorageRack rack
    ) {
        log.info("API: POST /api/racks — Rack #{}", rack.getRackNumber());
        StorageRack saved = inventoryService.addRack(rack);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Assign a part to a rack.
     *
     * UML Traceability:
     *   Class Diagram → StorageRack.assignPart()
     *
     * Example:
     *   PUT /api/parts/SP-BRK-001/rack/3
     */
    @PutMapping("/parts/{partNumber}/rack/{rackNumber}")
    @Operation(
        summary = "Assign part to rack",
        description = "Assigns a spare part to a specific wall rack"
    )
    public ResponseEntity<SparePart> assignPartToRack(
            @PathVariable String partNumber,
            @PathVariable Integer rackNumber
    ) {
        log.info("API: PUT /api/parts/{}/rack/{}", partNumber, rackNumber);
        return ResponseEntity.ok(inventoryService.assignPartToRack(partNumber, rackNumber));
    }
}