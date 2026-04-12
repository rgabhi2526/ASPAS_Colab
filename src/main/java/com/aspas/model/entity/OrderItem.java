package com.aspas.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @Column(nullable = false, length = 50)
    private String partNumber;

    @Column(length = 100)
    private String partName;

    @Column(nullable = false)
    @Min(value = 1, message = "Required quantity must be at least 1")
    private Integer requiredQuantity;

    @Column(length = 100)
    private String vendorName;

    @Column(nullable = false, length = 255)
    private String vendorAddress;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "part_id")
    private Long partId;

    @Column(name = "vendor_id")
    private Long vendorId;

    public String getItemDetails() {
        return String.format(
            "Part: %s [%s] | Qty: %d | Vendor: %s | Address: %s",
            partNumber, partName, requiredQuantity, vendorName, vendorAddress
        );
    }
}