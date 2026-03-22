package com.aspas.model.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * ================================================================
 * SalesTransactionDoc - MongoDB Document
 * ================================================================
 * 
 * UML Traceability:
 *   - Class Diagram: SalesTransaction class
 *   - Database: sales_transactions collection (MongoDB)
 *   - DFD Store: D2 Sales Log
 *   - Sequence Diagram: Message #6 "<<create>> SalesTransactionDoc"
 * 
 * High-write analytics collection. Every sale creates one document.
 * Used for:
 *   1. JIT threshold calculation (7-day rolling average)
 *   2. Daily revenue reports
 *   3. Monthly graph data aggregation
 * 
 * Stored in MongoDB (NoSQL) for:
 *   - Fast writes (high transaction frequency)
 *   - Flexible aggregation queries
 *   - Time-series analysis
 * 
 * Derived Attribute:
 *   - /revenueAmount = quantitySold × sellingPrice
 * 
 * ================================================================
 */
@Document(collection = "sales_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesTransactionDoc {

    @Id
    private String id;  // MongoDB _id (auto-generated)

    private String transactionId;       // Business key: TXN-20260315-001

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime transactionDate;

    private String partNumber;
    private String partName;

    private Integer quantitySold;
    private Double sellingPrice;

    // Derived Attribute: /revenueAmount
    // Computed from: quantitySold × sellingPrice
    // Stored here for quick queries (not normalization, but convenience)
    private Double revenueAmount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Constructor with computed derived attribute.
     * 
     * @param transactionId Business key
     * @param transactionDate Sale date/time
     * @param partNumber Part SKU
     * @param partName Part display name
     * @param quantitySold Units sold
     * @param sellingPrice Unit price
     */
    public SalesTransactionDoc(
        String transactionId,
        LocalDateTime transactionDate,
        String partNumber,
        String partName,
        Integer quantitySold,
        Double sellingPrice
    ) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.partNumber = partNumber;
        this.partName = partName;
        this.quantitySold = quantitySold;
        this.sellingPrice = sellingPrice;
        
        // Compute derived attribute
        this.revenueAmount = quantitySold * sellingPrice;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Derived Attribute: /revenueAmount
     * 
     * UML Traceability: Class Diagram → SalesTransaction /revenueAmount
     * Formula: quantitySold × sellingPrice
     * 
     * @return computed revenue
     */
    public Double getRevenueAmount() {
        return quantitySold != null && sellingPrice != null 
            ? quantitySold * sellingPrice 
            : 0.0;
    }

    /**
     * Log the sale (stores document in MongoDB).
     * 
     * UML Traceability: Sequence Diagram → Message #7 "ST.logSale()"
     * 
     * This method is called after the document is constructed.
     * Persistence happens in SaleService.
     */
    public void logSale() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    /**
     * Get transaction details as string.
     * @return formatted transaction info
     */
    public String getTransactionDetails() {
        return String.format(
            "[%s] %s - Part: %s, Qty: %d, Price: ₹%.2f, Revenue: ₹%.2f",
            transactionId, transactionDate, partNumber, quantitySold, sellingPrice, revenueAmount
        );
    }
}