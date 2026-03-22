package com.aspas.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * ================================================================
 * OrderItem Entity
 * ================================================================
 * 
 * UML Traceability:
 *   - Class Diagram: OrderItem class
 *   - Parent: OrderList 1 *── OrderItem (Composition)
 *   - Database: order_items table (MySQL)
 *   - Sequence Diagram: Message #21 "<<create>> OrderItem"
 * 
 * Represents one line item on an end-of-day order.
 * Example: "Part SP-BRK-001, Qty 5, from Vendor Bosch, Address ..."
 * 
 * This is a Composition child:
 *   - CANNOT exist without parent OrderList
 *   - Deleted when parent is deleted (ON DELETE CASCADE)
 * 
 * ================================================================
 */
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

    // Foreign keys (informational, not navigation)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "part_id")
    private Long partId;

    @Column(name = "vendor_id")
    private Long vendorId;

    /**
     * Get item details as formatted string.
     * 
     * @return Item information
     */
    public String getItemDetails() {
        return String.format(
            "Part: %s [%s] | Qty: %d | Vendor: %s | Address: %s",
            partNumber, partName, requiredQuantity, vendorName, vendorAddress
        );
    }
}