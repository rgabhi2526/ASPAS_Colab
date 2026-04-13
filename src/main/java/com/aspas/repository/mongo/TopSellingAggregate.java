package com.aspas.repository.mongo;

/**
 * Projection for top-selling parts aggregation. Top-level for Spring Data MongoDB.
 */
public interface TopSellingAggregate {
    String getId();             // partNumber
    String getPartName();       // part name
    Number getTotalQty();       // total quantity sold
    Double getTotalRevenue();   // total revenue from this part
}
