package com.aspas.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * ================================================================
 * StorageRack Entity
 * ================================================================
 * 
 * UML Traceability:
 *   - Class Diagram: StorageRack class
 *   - Relationship: SparePart *──1 StorageRack (stored in)
 *   - Database: storage_racks table (MySQL)
 * 
 * Represents a physical wall-mounted numbered rack where
 * spare parts are physically located.
 * 
 * ================================================================
 */
@Entity
@Table(name = "storage_racks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
public class StorageRack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rackId;

    @Column(nullable = false, unique = true)
    private Integer rackNumber;

    @Column(nullable = false, length = 50)
    private String wallLocation;

    @Column(nullable = false)
    private Integer maxCapacity = 100;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Get rack information as formatted string.
     * @return Rack details
     */
    public String getRackInfo() {
        return String.format(
            "Rack #%d [%s] - Capacity: %d units",
            rackNumber, wallLocation, maxCapacity
        );
    }
}