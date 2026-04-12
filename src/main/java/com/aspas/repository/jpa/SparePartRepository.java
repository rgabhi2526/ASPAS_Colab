package com.aspas.repository.jpa;

import com.aspas.model.entity.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * ================================================================
 * SparePartRepository — JPA (MySQL)
 * ================================================================
 *
 * UML Traceability:
 *   - Class Diagram  : SparePart entity
 *   - DFD Store      : D1 Inventory File
 *   - Sequence Diagram:
 *       Message #2-3 : getPartDetails()       → findByPartNumber()
 *       Message #4-5 : updateQuantity()        → save() after entity update
 *       Message #14  : updateThreshold()       → save() after entity update
 *       Message #17  : checkThreshold()        → findPartsBelow Threshold()
 *
 * Provides:
 *   - Standard CRUD via JpaRepository
 *   - Lookup by part number (business key)
 *   - JIT query: find all parts below threshold
 *   - Rack-based lookups
 *   - Search by name/category
 *
 * ================================================================
 */
@Repository
public interface SparePartRepository extends JpaRepository<SparePart, Long> {

    // ══════════════════════════════════════════
    //  BASIC LOOKUPS
    // ══════════════════════════════════════════

    /**
     * Find a spare part by its unique part number.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #2 "SC → SP : getPartDetails()"
     *   Used in: SaleService.processSale()
     *
     * @param partNumber unique business key (e.g., "SP-BRK-001")
     * @return Optional containing the part if found
     */
    Optional<SparePart> findByPartNumber(String partNumber);

    /**
     * Check if a part number already exists.
     *
     * @param partNumber business key to check
     * @return true if exists
     */
    boolean existsByPartNumber(String partNumber);

    /**
     * Find parts by name containing a keyword (case-insensitive search).
     *
     * @param keyword search term
     * @return matching parts
     */
    List<SparePart> findByPartNameContainingIgnoreCase(String keyword);

    /**
     * Find all parts in a specific size category.
     *
     * @param sizeCategory SMALL, MEDIUM, or LARGE
     * @return parts in that category
     */
    List<SparePart> findBySizeCategory(String sizeCategory);


    // ══════════════════════════════════════════
    //  JIT / THRESHOLD QUERIES
    // ══════════════════════════════════════════

    /**
     * Find all parts where current quantity is below the JIT threshold.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #17 "SC → SP : checkThreshold()"
     *   Use Case: UC-06 Check Inventory vs Threshold
     *   DFD: P3.0 reads from D1 to check qty < threshold
     *
     * This is the CORE JIT query — identifies what needs to be reordered.
     *
     * @return list of parts needing reorder
     */
    @Query("SELECT sp FROM SparePart sp WHERE sp.currentQuantity < sp.thresholdValue")
    List<SparePart> findPartsBelowThreshold();

    /**
     * Count how many parts are below threshold.
     *
     * @return count of parts needing reorder
     */
    @Query("SELECT COUNT(sp) FROM SparePart sp WHERE sp.currentQuantity < sp.thresholdValue")
    Long countPartsBelowThreshold();

    /**
     * Find parts where stock is zero (completely out of stock).
     *
     * @return out-of-stock parts
     */
    @Query("SELECT sp FROM SparePart sp WHERE sp.currentQuantity = 0")
    List<SparePart> findOutOfStockParts();


    // ══════════════════════════════════════════
    //  RACK-BASED QUERIES
    // ══════════════════════════════════════════

    /**
     * Find all parts stored in a specific rack.
     *
     * @param rackId rack identifier
     * @return parts in that rack
     */
    List<SparePart> findByStorageRack_RackId(Integer rackId);

    /**
     * Sum of {@code currentQuantity} for parts on a rack (excluding one part, e.g. during update).
     */
    @Query("SELECT COALESCE(SUM(sp.currentQuantity), 0) FROM SparePart sp WHERE sp.storageRack IS NOT NULL "
        + "AND sp.storageRack.rackId = :rackId "
        + "AND (:excludePartId IS NULL OR sp.partId <> :excludePartId)")
    Long sumCurrentQuantityOnRackExcluding(
        @Param("rackId") Integer rackId,
        @Param("excludePartId") Long excludePartId
    );

    List<SparePart> findByPartIdIn(Collection<Long> partIds);

    /**
     * Find all parts stored in a specific rack number.
     *
     * @param rackNumber physical rack number on the wall
     * @return parts in that rack
     */
    @Query("SELECT sp FROM SparePart sp WHERE sp.storageRack.rackNumber = :rackNumber")
    List<SparePart> findByRackNumber(@Param("rackNumber") Integer rackNumber);


    // ══════════════════════════════════════════
    //  BULK OPERATIONS
    // ══════════════════════════════════════════

    /**
     * Update threshold value for a specific part.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #14 "SC → SP : updateThreshold()"
     *   DFD: P2.0 writes to D1
     *
     * @param partNumber part to update
     * @param threshold new threshold value
     */
    @Modifying
    @Query("UPDATE SparePart sp SET sp.thresholdValue = :threshold, " +
           "sp.updatedAt = CURRENT_TIMESTAMP WHERE sp.partNumber = :partNumber")
    void updateThresholdByPartNumber(
        @Param("partNumber") String partNumber,
        @Param("threshold") Integer threshold
    );

    /**
     * Deduct quantity for a sale.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #4 "SC → SP : updateQuantity(-qty)"
     *
     * @param partNumber part sold
     * @param quantity amount to deduct
     */
    @Modifying
    @Query("UPDATE SparePart sp SET sp.currentQuantity = sp.currentQuantity - :quantity, " +
           "sp.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE sp.partNumber = :partNumber AND sp.currentQuantity >= :quantity")
    int deductQuantity(
        @Param("partNumber") String partNumber,
        @Param("quantity") Integer quantity
    );
}