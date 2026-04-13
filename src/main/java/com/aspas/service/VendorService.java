package com.aspas.service;

import com.aspas.exception.VendorNotFoundException;
import com.aspas.model.entity.SparePart;
import com.aspas.model.entity.Vendor;
import com.aspas.repository.jpa.SparePartRepository;
import com.aspas.repository.jpa.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * ================================================================
 * VendorService — Vendor Directory Management
 * ================================================================
 *
 * UML Traceability:
 *   - DFD Store: D3 Vendor Directory
 *   - Class Diagram: Vendor class
 *   - Sequence Diagram: Message #19-20 "SC → V : getVendorAddress()"
 *   - Use Case: UC-04 Fetch Vendor Address
 *
 * Manages vendor CRUD operations and provides vendor lookup
 * for the order generation process.
 *
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VendorService {

    private final VendorRepository vendorRepository;
    private final SparePartRepository sparePartRepository;

    // ══════════════════════════════════════════
    //  VENDOR CRUD
    // ══════════════════════════════════════════

    /**
     * Get all vendors.
     *
     * @return list of all vendors
     */
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    /**
     * Get a vendor by ID.
     *
     * @param vendorId database ID
     * @return the vendor
     * @throws VendorNotFoundException if not found
     */
    public Vendor getVendorById(Long vendorId) {
        return vendorRepository.findById(vendorId)
            .orElseThrow(() -> new VendorNotFoundException(vendorId));
    }

    /**
     * Get a vendor by name.
     *
     * @param vendorName vendor name
     * @return the vendor
     */
    public Vendor getVendorByName(String vendorName) {
        return vendorRepository.findByVendorName(vendorName)
            .orElseThrow(() -> new VendorNotFoundException(vendorName));
    }

    /**
     * Add a new vendor.
     *
     * @param vendor the vendor entity
     * @return saved entity
     */
    @Transactional
    public Vendor addVendor(Vendor vendor) {
        log.info("Adding vendor: {}", vendor.getVendorName());

        if (vendorRepository.existsByVendorName(vendor.getVendorName())) {
            throw new IllegalArgumentException(
                "Vendor already exists: " + vendor.getVendorName()
            );
        }

        return vendorRepository.save(vendor);
    }

    /**
     * Update a vendor.
     *
     * @param vendorId vendor to update
     * @param updatedVendor updated data
     * @return updated entity
     */
    @Transactional
    public Vendor updateVendor(Long vendorId, Vendor updatedVendor) {
        Vendor existing = getVendorById(vendorId);

        log.info("Updating vendor: {}", existing.getVendorName());

        existing.setVendorName(updatedVendor.getVendorName());
        existing.setVendorAddress(updatedVendor.getVendorAddress());
        existing.updateContactInfo(
            updatedVendor.getContactNumber(),
            updatedVendor.getEmail()
        );

        return vendorRepository.save(existing);
    }

    /**
     * Delete a vendor.
     *
     * @param vendorId vendor to delete
     */
    @Transactional
    public void deleteVendor(Long vendorId) {
        Vendor vendor = getVendorById(vendorId);
        log.info("Deleting vendor: {}", vendor.getVendorName());
        vendorRepository.delete(vendor);
    }

    /**
     * Search vendors by name keyword.
     *
     * @param keyword search term
     * @return matching vendors
     */
    public List<Vendor> searchVendors(String keyword) {
        return vendorRepository.findByVendorNameContainingIgnoreCase(keyword);
    }

    // ══════════════════════════════════════════
    //  ORDER GENERATION SUPPORT
    // ══════════════════════════════════════════

    /**
     * Get all vendors that supply a specific part.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #19 "SC → V : getVendorAddress()"
     *   UC-04: Fetch Vendor Address (<<include>> for UC-03)
     *   DFD: P3.0 reads from D3 Vendor Directory
     *
     * @param partId spare part database ID
     * @return list of vendors for that part
     */
    public List<Vendor> getVendorsForPart(Long partId) {
        log.debug("Fetching vendors for part ID: {}", partId);
        return vendorRepository.findVendorsForPart(partId);
    }

    /**
     * All spare parts supplied by a vendor (via {@code part_vendor} join table).
     *
     * @param vendorId vendor primary key
     * @return parts linked to that vendor (may be empty)
     */
    public List<SparePart> getPartsSuppliedByVendor(Long vendorId) {
        List<Long> partIds = vendorRepository.findPartIdsByVendor(vendorId);
        if (partIds == null || partIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sparePartRepository.findByPartIdIn(partIds);
    }

    /**
     * Get the PRIMARY vendor for a specific part.
     *
     * The primary vendor is the preferred supplier marked in the
     * part_vendor join table (is_primary = TRUE).
     *
     * UML Traceability:
     *   Sequence Diagram → Message #19-20
     *   Used during order generation to select which vendor to order from
     *
     * @param partId spare part database ID
     * @return the primary vendor
     */
    public Optional<Vendor> getPrimaryVendorForPart(Long partId) {
        log.debug("Fetching primary vendor for part ID: {}", partId);
        return vendorRepository.findPrimaryVendorForPart(partId);
    }

    /**
     * Get vendor address for a part (used during order generation).
     *
     * Falls back to the first available vendor if no primary is set.
     *
     * @param partId spare part ID
     * @return vendor with address
     */
    public Vendor getVendorForOrder(Long partId) {
        // Try primary vendor first
        Optional<Vendor> primary = getPrimaryVendorForPart(partId);
        if (primary.isPresent()) {
            return primary.get();
        }

        // Fallback: get any vendor for this part
        List<Vendor> vendors = getVendorsForPart(partId);
        if (vendors.isEmpty()) {
            throw VendorNotFoundException.forPart("Part ID: " + partId);
        }

        log.warn("No primary vendor for part {}, using first available: {}",
            partId, vendors.get(0).getVendorName());
        return vendors.get(0);
    }
}