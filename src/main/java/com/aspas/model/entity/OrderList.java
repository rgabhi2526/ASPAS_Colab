package com.aspas.model.entity;

import com.aspas.model.interfaces.Printable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        totalItems = orderItems.size();
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        totalItems = orderItems.size();
    }

    public Integer getTotalItemsCount() {
        return orderItems.size();
    }

    public String getOrderSummary() {
        return String.format(
            "Order #%d | Date: %s | Total Items: %d | Printed: %s",
            orderId, orderDate, getTotalItemsCount(), isPrinted
        );
    }

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

    @Override
    public void print() {
        this.printText = formatOutput();
        this.isPrinted = true;
        
        System.out.println("\n" + this.printText);
    }
}