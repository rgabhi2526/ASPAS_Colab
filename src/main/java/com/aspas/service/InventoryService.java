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

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final SparePartRepository sparePartRepository;
    private final StorageRackRepository storageRackRepository;

    public List<SparePart> getAllParts() {
        log.debug("Fetching all spare parts");
        return sparePartRepository.findAll();
    }

    public SparePart getPartByNumber(String partNumber) {
        log.debug("Looking up part: {}", partNumber);
        return sparePartRepository.findByPartNumber(partNumber)
            .orElseThrow(() -> new PartNotFoundException(partNumber));
    }

    public SparePart getPartById(Long partId) {
        return sparePartRepository.findById(partId)
            .orElseThrow(() -> new PartNotFoundException(partId));
    }

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

    @Transactional
    public void deletePart(String partNumber) {
        SparePart part = getPartByNumber(partNumber);
        log.info("Deleting part: {}", partNumber);
        sparePartRepository.delete(part);
    }

    public List<SparePart> searchParts(String keyword) {
        return sparePartRepository.findByPartNameContainingIgnoreCase(keyword);
    }

    public List<SparePart> getPartsBelowThreshold() {
        log.debug("Checking parts below JIT threshold");
        return sparePartRepository.findPartsBelowThreshold();
    }

    public List<SparePart> getPartsByRack(Integer rackNumber) {
        return sparePartRepository.findByRackNumber(rackNumber);
    }

    public List<StorageRack> getAllRacks() {
        return storageRackRepository.findAllByOrderByRackNumberAsc();
    }

    public StorageRack getRackByNumber(Integer rackNumber) {
        return storageRackRepository.findByRackNumber(rackNumber)
            .orElseThrow(() -> new RuntimeException("Rack not found: " + rackNumber));
    }

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

    @Transactional
    public SparePart assignPartToRack(String partNumber, Integer rackNumber) {
        SparePart part = getPartByNumber(partNumber);
        StorageRack rack = getRackByNumber(rackNumber);

        log.info("Assigning part {} to rack #{}", partNumber, rackNumber);

        part.setStorageRack(rack);
        return sparePartRepository.save(part);
    }
}