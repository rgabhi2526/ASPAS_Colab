package com.aspas.model.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "sales_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesTransactionDoc {

    @Id
    private String id;  

    private String transactionId;       

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime transactionDate;

    private String partNumber;
    private String partName;

    private Integer quantitySold;
    private Double sellingPrice;

    private Double revenueAmount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

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

        this.revenueAmount = quantitySold * sellingPrice;
        this.createdAt = LocalDateTime.now();
    }

    public Double getRevenueAmount() {
        return quantitySold != null && sellingPrice != null 
            ? quantitySold * sellingPrice 
            : 0.0;
    }

    public void logSale() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public String getTransactionDetails() {
        return String.format(
            "[%s] %s - Part: %s, Qty: %d, Price: ₹%.2f, Revenue: ₹%.2f",
            transactionId, transactionDate, partNumber, quantitySold, sellingPrice, revenueAmount
        );
    }
}