package com.aspas.model.entity;

import com.aspas.model.interfaces.Printable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * OrderList Entity
 * ================================================================
 * 
 * UML Traceability:
 *   - Class Diagram: OrderList class
 *   - Interface: Implements Printable
 *   - Relationship: OrderList 1 *── OrderItem (Composition)
 *   - Database: order_lists table (MySQL)
 *   - Sequence Diagram: Message #15 "<<create>> OrderList"
 * 
 * Represents a daily end-of-day order list sent to vendors.
 * Contains multiple OrderItems (one line per part-vendor combo).
 * 
 * Composition: OrderItems CANNOT exist without their parent OrderList.
 * ON DELETE CASCADE ensures this relationship.
 * 
 * ================================================================
 */
@Entity
@Table(name = "order_lists")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
public class OrderList implements Printable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(nullable = false)
    private Integer totalItems = 0;

    @Column(nullable = false)
    private Boolean isPrinted = false;

    @Column(columnDefinition = "TEXT")
    private String printText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Composition: OrderItems are owned by OrderList
    // CASCADE delete ensures items are deleted when list is deleted
    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Add an order item (one line on the order).
     * 
     * UML Traceability: Sequence Diagram → Message #22
     * 
     * @param item OrderItem to add
     */
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        totalItems = orderItems.size();
    }

    /**
     * Remove an order item.
     */
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        totalItems = orderItems.size();
    }

    /**
     * Derived Attribute: /totalItems
     * Always computed from the size of the orderItems list.
     * 
     * @return number of distinct order items
     */
    public Integer getTotalItemsCount() {
        return orderItems.size();
    }

    /**
     * Get order summary (for API responses).
     * @return summary string
     */
    public String getOrderSummary() {
        return String.format(
            "Order #%d | Date: %s | Total Items: %d | Printed: %s",
            orderId, orderDate, getTotalItemsCount(), isPrinted
        );
    }

    /**
     * Format output as print-ready text.
     * 
     * UML Traceability: 
     *   - Implements Printable interface
     *   - Sequence Diagram → Message #23 "OL.print()"
     * 
     * @return formatted order text
     */
    @Override
    public String formatOutput() {
        StringBuilder output = new StringBuilder();
        
        output.append("════════════════════════════════════════════════════════\n");
        output.append("            ASPAS - END OF DAY ORDER LIST\n");
        output.append("════════════════════════════════════════════════════════\n");
        output.append(String.format("Order Date: %s\n", orderDate));
        output.append(String.format("Order ID: %d\n", orderId));
        output.append("────────────────────────────────────────────────────────\n");
        output.append("LINE# | PART# | PART NAME | QTY | VENDOR | ADDRESS\n");
        output.append("────────────────────────────────────────────────────────\n");

        int lineNo = 1;
        for (OrderItem item : orderItems) {
            output.append(String.format(
                "%5d | %-5s | %-20s | %3d | %-25s | %s\n",
                lineNo++,
                item.getPartNumber(),
                item.getPartName() != null ? item.getPartName() : "N/A",
                item.getRequiredQuantity(),
                item.getVendorName() != null ? item.getVendorName() : "N/A",
                item.getVendorAddress()
            ));
        }

        output.append("────────────────────────────────────────────────────────\n");
        output.append(String.format("Total Items to Order: %d\n", getTotalItemsCount()));
        output.append("────────────────────────────────────────────────────────\n");
        output.append(String.format("Generated: %s\n", createdAt));
        output.append("════════════════════════════════════════════════════════\n");

        return output.toString();
    }

    /**
     * Print the order list.
     * 
     * UML Traceability: Implements Printable interface
     * 
     * Backend: stores print text for later retrieval
     * (No actual printer integration in this phase)
     */
    @Override
    public void print() {
        this.printText = formatOutput();
        this.isPrinted = true;
        
        System.out.println("\n" + this.printText);
    }
}