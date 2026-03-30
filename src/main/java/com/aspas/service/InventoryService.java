package com.aspas.service;

import com.aspas.exception.PartNotFoundException;
import com.aspas.model.entity.SparePart;
import com.aspas.model.entity.StorageRack;
import com.aspas.repository.jpa.SparePartRepository;
import com.aspas.repository.jpa.StorageRackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * ================================================================
 * InventoryService — Inventory CRUD & Management
 * ================================================================
 *
 * UML Traceability:
 *   - DFD Store: D1 Inventory File
 *   - Class Diagram: SparePart, StorageRack entities
 *   - Provides CRUD operations on the Inventory
 *
 * Manages spare parts and storage racks.
 *
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final SparePartRepository sparePartRepository;
    private final StorageRackRepository storageRackRepository;

    // ══════════════════════════════════════════
    //  SPARE PARTS CRUD
    // ══════════════════════════════════════════

    /**
     * Get all spare parts.
     *
     * @return all parts
     */
    public List<SparePart> getAllParts() {
        log.debug("Fetching all spare parts");
        return sparePartRepository.findAll();
    }

    /**
     * Get a spare part by its part number.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #2-3 "SC → SP : getPartDetails()"
     *
     * @param partNumber business key
     * @return the spare part
     * @throws PartNotFoundException if not found
     */
    public SparePart getPartByNumber(String partNumber) {
        log.debug("Looking up part: {}", partNumber);
        return sparePartRepository.findByPartNumber(partNumber)
            .orElseThrow(() -> new PartNotFoundException(partNumber));
    }

    /**
     * Get a spare part by its database ID.
     *
     * @param partId database ID
     * @return the spare part
     * @throws PartNotFoundException if not found
     */
    public SparePart getPartById(Long partId) {
        return sparePartRepository.findById(partId)
            .orElseThrow(() -> new PartNotFoundException(partId));
    }

    /**
     * Add a new spare part to inventory.
     *
     * @param part the spare part entity
     * @return saved entity
     */
    @Transactional
    public SparePart addPart(SparePart part) {
        log.info("Adding new part: {} [{}]", part.getPartNumber(), part.getPartName());

        if (sparePartRepository.existsByPartNumber(part.getPartNumber())) {
            throw new IllegalArgumentException(
                "Part number already exists: " + part.getPartNumber()
            );
        }

        return sparePartRepository.save(part);
    }

    /**
     * Update an existing spare part.
     *
     * @param partNumber part to update
     * @param updatedPart updated data
     * @return updated entity
     */
    @Transactional
    public SparePart updatePart(String partNumber, SparePart updatedPart) {
        SparePart existing = getPartByNumber(partNumber);

        log.info("Updating part: {}", partNumber);

        existing.setPartName(updatedPart.getPartName());
        existing.setUnitPrice(updatedPart.getUnitPrice());
        existing.setSizeCategory(updatedPart.getSizeCategory());

        if (updatedPart.getCurrentQuantity() != null) {
            existing.setCurrentQuantity(updatedPart.getCurrentQuantity());
        }

        return sparePartRepository.save(existing);
    }

    /**
     * Delete a spare part from inventory.
     *
     * @param partNumber part to delete
     */
    @Transactional
    public void deletePart(String partNumber) {
        SparePart part = getPartByNumber(partNumber);
        log.info("Deleting part: {}", partNumber);
        sparePartRepository.delete(part);
    }

    /**
     * Search parts by name keyword.
     *
     * @param keyword search term
     * @return matching parts
     */
    public List<SparePart> searchParts(String keyword) {
        return sparePartRepository.findByPartNameContainingIgnoreCase(keyword);
    }

    /**
     * Find parts below JIT threshold (needing reorder).
     *
     * UML Traceability:
     *   UC-06: Check Inventory vs Threshold
     *   Sequence Diagram → Message #17
     *
     * @return parts below threshold
     */
    public List<SparePart> getPartsBelowThreshold() {
        log.debug("Checking parts below JIT threshold");
        return sparePartRepository.findPartsBelowThreshold();
    }

    /**
     * Find parts stored in a specific rack.
     *
     * @param rackNumber physical rack number
     * @return parts in that rack
     */
    public List<SparePart> getPartsByRack(Integer rackNumber) {
        return sparePartRepository.findByRackNumber(rackNumber);
    }

    // ══════════════════════════════════════════
    //  STORAGE RACKS CRUD
    // ══════════════════════════════════════════

    /**
     * Get all storage racks.
     *
     * @return all racks sorted by number
     */
    public List<StorageRack> getAllRacks() {
        return storageRackRepository.findAllByOrderByRackNumberAsc();
    }

    /**
     * Get a rack by its number.
     *
     * @param rackNumber physical rack number
     * @return the rack
     */
    public StorageRack getRackByNumber(Integer rackNumber) {
        return storageRackRepository.findByRackNumber(rackNumber)
            .orElseThrow(() -> new RuntimeException("Rack not found: " + rackNumber));
    }

    /**
     * Add a new rack.
     *
     * @param rack the rack entity
     * @return saved entity
     */
    @Transactional
    public StorageRack addRack(StorageRack rack) {
        log.info("Adding rack #{} at {}", rack.getRackNumber(), rack.getWallLocation());

        if (storageRackRepository.existsByRackNumber(rack.getRackNumber())) {
            throw new IllegalArgumentException(
                "Rack number already exists: " + rack.getRackNumber()
            );
        }

        return storageRackRepository.save(rack);
    }

    /**
     * Assign a part to a specific rack.
     *
     * UML Traceability:
     *   Class Diagram → StorageRack.assignPart()
     *
     * @param partNumber part to assign
     * @param rackNumber target rack
     * @return updated part
     */
    @Transactional
    public SparePart assignPartToRack(String partNumber, Integer rackNumber) {
        SparePart part = getPartByNumber(partNumber);
        StorageRack rack = getRackByNumber(rackNumber);

        log.info("Assigning part {} to rack #{}", partNumber, rackNumber);

        part.setStorageRack(rack);
        return sparePartRepository.save(part);
    }
}