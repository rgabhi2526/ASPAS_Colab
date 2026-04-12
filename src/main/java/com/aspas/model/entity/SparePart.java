package com.aspas.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "spare_parts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
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
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
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

    public boolean isBelowThreshold() {
        return currentQuantity < thresholdValue;
    }

    public boolean checkThreshold() {
        return isBelowThreshold();
    }

    public void updateQuantity(int amount) {
        this.currentQuantity += amount;
        if (this.currentQuantity < 0) {
            this.currentQuantity = 0;
        }
    }

    public void updateThreshold(int newThreshold) {
        this.thresholdValue = newThreshold;
    }

    public String getPartDetails() {
        return String.format(
            "Part: %s [%s] | Stock: %d | Threshold: %d | Price: ₹%.2f | Location: %s",
            partNumber, partName, currentQuantity, thresholdValue, unitPrice,
            storageRack != null ? "Rack #" + storageRack.getRackNumber() : "UNASSIGNED"
        );
    }
}