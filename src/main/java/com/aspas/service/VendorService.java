package com.aspas.service;

import com.aspas.exception.VendorNotFoundException;
import com.aspas.model.entity.Vendor;
import com.aspas.repository.jpa.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorService {

    private final VendorRepository vendorRepository;

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public Vendor getVendorById(Long vendorId) {
        return vendorRepository.findById(vendorId)
            .orElseThrow(() -> new VendorNotFoundException(vendorId));
    }

    public Vendor getVendorByName(String vendorName) {
        return vendorRepository.findByVendorName(vendorName)
            .orElseThrow(() -> new VendorNotFoundException(vendorName));
    }

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

    @Transactional
    public void deleteVendor(Long vendorId) {
        Vendor vendor = getVendorById(vendorId);
        log.info("Deleting vendor: {}", vendor.getVendorName());
        vendorRepository.delete(vendor);
    }

    public List<Vendor> searchVendors(String keyword) {
        return vendorRepository.findByVendorNameContainingIgnoreCase(keyword);
    }

    public List<Vendor> getVendorsForPart(Long partId) {
        log.debug("Fetching vendors for part ID: {}", partId);
        return vendorRepository.findVendorsForPart(partId);
    }

    public Optional<Vendor> getPrimaryVendorForPart(Long partId) {
        log.debug("Fetching primary vendor for part ID: {}", partId);
        return vendorRepository.findPrimaryVendorForPart(partId);
    }

    public Vendor getVendorForOrder(Long partId) {
        
        Optional<Vendor> primary = getPrimaryVendorForPart(partId);
        if (primary.isPresent()) {
            return primary.get();
        }

        List<Vendor> vendors = getVendorsForPart(partId);
        if (vendors.isEmpty()) {
            throw VendorNotFoundException.forPart("Part ID: " + partId);
        }

        log.warn("No primary vendor for part {}, using first available: {}",
            partId, vendors.get(0).getVendorName());
        return vendors.get(0);
    }
}