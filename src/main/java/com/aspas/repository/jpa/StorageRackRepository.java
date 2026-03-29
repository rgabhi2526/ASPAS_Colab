package com.aspas.repository.jpa;

import com.aspas.model.entity.StorageRack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * ================================================================
 * StorageRackRepository — JPA (MySQL)
 * ================================================================
 *
 * UML Traceability:
 *   - Class Diagram  : StorageRack class
 *   - Relationship   : SparePart *──1 StorageRack (stored in)
 *
 * Provides:
 *   - Standard CRUD via JpaRepository
 *   - Lookup by rack number (physical identification)
 *   - Location-based queries
 *   - Capacity management
 *
 * ================================================================
 */
@Repository
public interface StorageRackRepository extends JpaRepository<StorageRack, Integer> {

    /**
     * Find a rack by its physical rack number.
     *
     * @param rackNumber wall-mounted rack number
     * @return Optional containing the rack if found
     */
    Optional<StorageRack> findByRackNumber(Integer rackNumber);

    /**
     * Check if a rack number already exists.
     *
     * @param rackNumber rack number to check
     * @return true if exists
     */
    boolean existsByRackNumber(Integer rackNumber);

    /**
     * Find racks by wall location keyword.
     *
     * @param location partial location name (e.g., "North Wall")
     * @return matching racks
     */
    List<StorageRack> findByWallLocationContainingIgnoreCase(String location);

    /**
     * Find racks with available capacity.
     * A rack has capacity if fewer parts are stored than maxCapacity.
     *
     * @param minCapacity minimum available space required
     * @return racks with at least minCapacity available
     */
    @Query("SELECT sr FROM StorageRack sr WHERE sr.maxCapacity >= :minCapacity")
    List<StorageRack> findRacksWithCapacity(@Param("minCapacity") Integer minCapacity);

    /**
     * Get all racks ordered by rack number.
     *
     * @return racks sorted ascending
     */
    List<StorageRack> findAllByOrderByRackNumberAsc();
}