package com.aspas.repository.jpa;

import com.aspas.model.entity.StorageRack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StorageRackRepository extends JpaRepository<StorageRack, Integer> {

    Optional<StorageRack> findByRackNumber(Integer rackNumber);

    boolean existsByRackNumber(Integer rackNumber);

    List<StorageRack> findByWallLocationContainingIgnoreCase(String location);

    @Query("SELECT sr FROM StorageRack sr WHERE sr.maxCapacity >= :minCapacity")
    List<StorageRack> findRacksWithCapacity(@Param("minCapacity") Integer minCapacity);

    List<StorageRack> findAllByOrderByRackNumberAsc();
}