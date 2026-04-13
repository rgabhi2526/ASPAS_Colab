package com.aspas.service;

import com.aspas.exception.PartNotFoundException;
import com.aspas.model.dto.SparePartRequestDTO;
import com.aspas.model.entity.SparePart;
import com.aspas.model.entity.StorageRack;
import com.aspas.model.entity.Vendor;
import com.aspas.repository.jpa.SparePartRepository;
import com.aspas.repository.jpa.StorageRackRepository;
import com.aspas.repository.jpa.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final SparePartRepository sparePartRepository;
    private final StorageRackRepository storageRackRepository;
    private final VendorRepository vendorRepository;

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
    public SparePart addPart(SparePartRequestDTO dto) {
        log.info("Adding new part: {} [{}]", dto.getPartNumber(), dto.getPartName());

        if (sparePartRepository.existsByPartNumber(dto.getPartNumber())) {
            throw new IllegalArgumentException(
                "Part number already exists: " + dto.getPartNumber()
            );
        }

        SparePart part = SparePart.builder()
            .partNumber(dto.getPartNumber())
            .partName(dto.getPartName())
            .currentQuantity(dto.getCurrentQuantity() != null ? dto.getCurrentQuantity() : 0)
            .thresholdValue(dto.getThresholdValue() != null ? dto.getThresholdValue() : 0)
            .unitPrice(dto.getUnitPrice())
            .sizeCategory(dto.getSizeCategory() != null ? dto.getSizeCategory() : "MEDIUM")
            .build();

        if (dto.getVendorIds() != null && !dto.getVendorIds().isEmpty()) {
            List<Vendor> vendors = new ArrayList<>();
            for (Long vendorId : dto.getVendorIds()) {
                vendors.add(vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + vendorId)));
            }
            part.setVendors(vendors);
            log.info("Linked {} vendor(s) to part {}", vendors.size(), dto.getPartNumber());
        }

        return sparePartRepository.save(part);
    }

    @Transactional
    public SparePart updatePart(String partNumber, SparePart updatedPart) {
        SparePart existing = getPartByNumber(partNumber);

        log.info("Updating part: {}", partNumber);

        StorageRack proposedRack = updatedPart.getStorageRack() != null
            ? resolveStorageRack(updatedPart.getStorageRack())
            : existing.getStorageRack();
        int proposedQty = updatedPart.getCurrentQuantity() != null
            ? updatedPart.getCurrentQuantity()
            : (existing.getCurrentQuantity() != null ? existing.getCurrentQuantity() : 0);

        assertRackHasCapacity(proposedRack, proposedQty, existing.getPartId());

        if (updatedPart.getPartName() != null) {
            existing.setPartName(updatedPart.getPartName());
        }
        if (updatedPart.getUnitPrice() != null) {
            existing.setUnitPrice(updatedPart.getUnitPrice());
        }
        if (updatedPart.getSizeCategory() != null) {
            existing.setSizeCategory(updatedPart.getSizeCategory());
        }
        if (updatedPart.getCurrentQuantity() != null) {
            existing.setCurrentQuantity(updatedPart.getCurrentQuantity());
        }
        if (updatedPart.getThresholdValue() != null) {
            existing.setThresholdValue(updatedPart.getThresholdValue());
        }
        if (updatedPart.getStorageRack() != null) {
            existing.setStorageRack(proposedRack);
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

        int qty = part.getCurrentQuantity() != null ? part.getCurrentQuantity() : 0;
        assertRackHasCapacity(rack, qty, part.getPartId());
        part.setStorageRack(rack);
        return sparePartRepository.save(part);
    }

    /**
     * Load a managed {@link StorageRack} from {@code rackId} or {@code rackNumber} sent on the wire.
     */
    private StorageRack resolveStorageRack(StorageRack rack) {
        if (rack == null) {
            return null;
        }
        if (rack.getRackId() != null) {
            return storageRackRepository.findById(rack.getRackId())
                .orElseThrow(() -> new IllegalArgumentException("Rack not found: id=" + rack.getRackId()));
        }
        if (rack.getRackNumber() != null) {
            return storageRackRepository.findByRackNumber(rack.getRackNumber())
                .orElseThrow(() -> new IllegalArgumentException("Rack not found: #" + rack.getRackNumber()));
        }
        throw new IllegalArgumentException("Storage rack must include rackId or rackNumber");
    }

    /**
     * Ensures total units on the rack (other parts + this part's proposed quantity) does not exceed {@code maxCapacity}.
     *
     * @param excludePartId part being updated/moved — its current row is excluded from the sum, then {@code proposedQuantity} is applied
     */
    private void assertRackHasCapacity(StorageRack rack, int proposedQuantity, Long excludePartId) {
        if (rack == null) {
            return;
        }
        StorageRack full = rack.getRackId() != null
            ? storageRackRepository.findById(rack.getRackId())
                .orElseThrow(() -> new IllegalArgumentException("Rack not found: id=" + rack.getRackId()))
            : resolveStorageRack(rack);
        int max = full.getMaxCapacity() != null ? full.getMaxCapacity() : 100;
        long others = sparePartRepository.sumCurrentQuantityOnRackExcluding(full.getRackId(), excludePartId);
        long total = others + (long) Math.max(0, proposedQuantity);
        if (total > max) {
            throw new IllegalArgumentException(String.format(
                "Rack #%d exceeds capacity: %d units on rack + %d proposed > max %d",
                full.getRackNumber(), others, proposedQuantity, max
            ));
        }
    }
}