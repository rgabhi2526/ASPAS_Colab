package com.aspas.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ================================================================
 * SparePart Entity
 * ================================================================
 * 
 * UML Traceability:
 *   - Class Diagram: SparePart class
 *   - Relationships:
 *     * SparePart *──1 StorageRack (stored in)
 *     * SparePart *──1..* Vendor (supplied by, via part_vendor join table)
 *   - Database: spare_parts table (MySQL)
 *   - DFD Store: D1 Inventory File
 * 
 * Core inventory entity. Tracks:
 *   - Physical stock quantity
 *   - Dynamic JIT threshold (7-day sales average)
 *   - Location (rack number)
 *   - Suppliers (vendors)
 * 
 * Derived Attribute:
 *   - /isBelowThreshold = (currentQuantity < thresholdValue)
 * 
 * ================================================================
 */
@Entity
@Table(name = "spare_parts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long partId;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Part number cannot be blank")
    private String partNumber;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Part name cannot be blank")
    private String partName;

    @Column(nullable = false)
    @Min(value = 0, message = "Current quantity cannot be negative")
    private Integer currentQuantity = 0;

    @Column(nullable = false)
    @Min(value = 0, message = "Threshold cannot be negative")
    private Integer thresholdValue = 0;

    @Column(nullable = false)
    private Double unitPrice;

    @Column(length = 20)
    private String sizeCategory = "MEDIUM";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rack_id")
    private StorageRack storageRack;

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
     * Derived Attribute: /isBelowThreshold
     * 
     * UML Traceability: Class Diagram → SparePart /isBelowThreshold
     * Computed from: currentQuantity < thresholdValue
     * Never stored, always computed on demand
     * 
     * @return true if current stock is below JIT threshold
     */
    public boolean isBelowThreshold() {
        return currentQuantity < thresholdValue;
    }

    /**
     * Check if part is below threshold.
     * 
     * UML Traceability: Sequence Diagram → Message #17
     * UC-06: Check Inventory vs Threshold
     * 
     * @return true if reorder is needed
     */
    public boolean checkThreshold() {
        return isBelowThreshold();
    }

    /**
     * Update stock quantity (for sales).
     * 
     * UML Traceability: Sequence Diagram → Message #4-5
     * UC-01: Process Sale & Update Inventory
     * 
     * @param amount amount to deduct (negative for sales)
     */
    public void updateQuantity(int amount) {
        this.currentQuantity += amount;
        if (this.currentQuantity < 0) {
            this.currentQuantity = 0;
        }
    }

    /**
     * Update JIT threshold value.
     * 
     * UML Traceability: Sequence Diagram → Message #14
     * DFD P2.0: Calculate JIT Thresholds
     * 
     * @param newThreshold new threshold based on 7-day average
     */
    public void updateThreshold(int newThreshold) {
        this.thresholdValue = newThreshold;
    }

    /**
     * Get part details as formatted string.
     * @return Part information
     */
    public String getPartDetails() {
        return String.format(
            "Part: %s [%s] | Stock: %d | Threshold: %d | Price: ₹%.2f | Location: %s",
            partNumber, partName, currentQuantity, thresholdValue, unitPrice,
            storageRack != null ? "Rack #" + storageRack.getRackNumber() : "UNASSIGNED"
        );
    }
}