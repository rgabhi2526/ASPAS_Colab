package com.aspas.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vendorId;

    @Column(nullable = false, length = 100)
    private String vendorName;

    @Column(nullable = false, length = 255)
    private String vendorAddress;

    @Column(length = 20)
    private String contactNumber;

    @Column(length = 100)
    private String email;

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

    public String getVendorAddress() {
        return vendorAddress;
    }

    public String getVendorDetails() {
        return String.format(
            "Vendor: %s | Address: %s | Contact: %s | Email: %s",
            vendorName, vendorAddress, contactNumber, email
        );
    }

    public void updateContactInfo(String phone, String emailAddr) {
        this.contactNumber = phone;
        this.email = emailAddr;
    }
}