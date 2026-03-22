package com.aspas.repository.jpa;

import com.aspas.model.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * ================================================================
 * VendorRepository — JPA (MySQL)
 * ================================================================
 *
 * UML Traceability:
 *   - Class Diagram  : Vendor class
 *   - DFD Store      : D3 Vendor Directory
 *   - Sequence Diagram:
 *       Message #19-20 : "SC → V : getVendorAddress()" → findVendorsForPart()
 *   - Use Case: UC-04 Fetch Vendor Address (<<include>> for UC-03)
 *
 * Provides:
 *   - Standard CRUD via JpaRepository
 *   - Lookup by vendor name
 *   - Find vendors supplying a specific part (many-to-many via part_vendor)
 *   - Find primary vendor for a part
 *
 * ================================================================
 */
@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    /**
     * Find vendor by name.
     *
     * @param vendorName vendor name
     * @return Optional containing the vendor if found
     */
    Optional<Vendor> findByVendorName(String vendorName);

    /**
     * Search vendors by name keyword.
     *
     * @param keyword partial name
     * @return matching vendors
     */
    List<Vendor> findByVendorNameContainingIgnoreCase(String keyword);

    /**
     * Check if a vendor name already exists.
     *
     * @param vendorName name to check
     * @return true if exists
     */
    boolean existsByVendorName(String vendorName);

    /**
     * Find ALL vendors that supply a specific part.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #19 "SC → V : getVendorAddress()"
     *   Use Case: UC-04 Fetch Vendor Address
     *   DFD: P3.0 reads from D3 Vendor Directory
     *
     * Uses the part_vendor join table (many-to-many relationship).
     *
     * @param partId spare part ID
     * @return list of vendors supplying this part
     */
    @Query(value = "SELECT v.* FROM vendors v " +
                   "INNER JOIN part_vendor pv ON v.vendor_id = pv.vendor_id " +
                   "WHERE pv.part_id = :partId",
           nativeQuery = true)
    List<Vendor> findVendorsForPart(@Param("partId") Long partId);

    /**
     * Find the PRIMARY vendor for a specific part.
     *
     * In the part_vendor join table, is_primary = TRUE marks
     * the preferred/primary supplier.
     *
     * @param partId spare part ID
     * @return Optional containing the primary vendor
     */
    @Query(value = "SELECT v.* FROM vendors v " +
                   "INNER JOIN part_vendor pv ON v.vendor_id = pv.vendor_id " +
                   "WHERE pv.part_id = :partId AND pv.is_primary = TRUE " +
                   "LIMIT 1",
           nativeQuery = true)
    Optional<Vendor> findPrimaryVendorForPart(@Param("partId") Long partId);

    /**
     * Find all parts supplied by a specific vendor.
     * Returns part IDs (for service layer to resolve).
     *
     * @param vendorId vendor ID
     * @return list of part IDs
     */
    @Query(value = "SELECT pv.part_id FROM part_vendor pv " +
                   "WHERE pv.vendor_id = :vendorId",
           nativeQuery = true)
    List<Long> findPartIdsByVendor(@Param("vendorId") Long vendorId);
}