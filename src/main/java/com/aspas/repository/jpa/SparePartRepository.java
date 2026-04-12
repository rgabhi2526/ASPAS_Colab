package com.aspas.repository.jpa;

import com.aspas.model.entity.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SparePartRepository extends JpaRepository<SparePart, Long> {

    Optional<SparePart> findByPartNumber(String partNumber);

    boolean existsByPartNumber(String partNumber);

    List<SparePart> findByPartNameContainingIgnoreCase(String keyword);

    List<SparePart> findBySizeCategory(String sizeCategory);

    @Query("SELECT sp FROM SparePart sp WHERE sp.currentQuantity < sp.thresholdValue")
    List<SparePart> findPartsBelowThreshold();

    @Query("SELECT COUNT(sp) FROM SparePart sp WHERE sp.currentQuantity < sp.thresholdValue")
    Long countPartsBelowThreshold();

    @Query("SELECT sp FROM SparePart sp WHERE sp.currentQuantity = 0")
    List<SparePart> findOutOfStockParts();

    List<SparePart> findByStorageRack_RackId(Integer rackId);

    @Query("SELECT sp FROM SparePart sp WHERE sp.storageRack.rackNumber = :rackNumber")
    List<SparePart> findByRackNumber(@Param("rackNumber") Integer rackNumber);

    @Modifying
    @Query("UPDATE SparePart sp SET sp.thresholdValue = :threshold, " +
           "sp.updatedAt = CURRENT_TIMESTAMP WHERE sp.partNumber = :partNumber")
    void updateThresholdByPartNumber(
        @Param("partNumber") String partNumber,
        @Param("threshold") Integer threshold
    );

    @Modifying
    @Query("UPDATE SparePart sp SET sp.currentQuantity = sp.currentQuantity - :quantity, " +
           "sp.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE sp.partNumber = :partNumber AND sp.currentQuantity >= :quantity")
    int deductQuantity(
        @Param("partNumber") String partNumber,
        @Param("quantity") Integer quantity
    );
}