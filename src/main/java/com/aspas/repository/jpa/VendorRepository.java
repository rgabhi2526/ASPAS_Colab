package com.aspas.repository.jpa;

import com.aspas.model.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByVendorName(String vendorName);

    List<Vendor> findByVendorNameContainingIgnoreCase(String keyword);

    boolean existsByVendorName(String vendorName);

    @Query(value = "SELECT v.* FROM vendors v " +
                   "INNER JOIN part_vendor pv ON v.vendor_id = pv.vendor_id " +
                   "WHERE pv.part_id = :partId",
           nativeQuery = true)
    List<Vendor> findVendorsForPart(@Param("partId") Long partId);

    @Query(value = "SELECT v.* FROM vendors v " +
                   "INNER JOIN part_vendor pv ON v.vendor_id = pv.vendor_id " +
                   "WHERE pv.part_id = :partId AND pv.is_primary = TRUE " +
                   "LIMIT 1",
           nativeQuery = true)
    Optional<Vendor> findPrimaryVendorForPart(@Param("partId") Long partId);

    @Query(value = "SELECT pv.part_id FROM part_vendor pv " +
                   "WHERE pv.vendor_id = :vendorId",
           nativeQuery = true)
    List<Long> findPartIdsByVendor(@Param("vendorId") Long vendorId);
}