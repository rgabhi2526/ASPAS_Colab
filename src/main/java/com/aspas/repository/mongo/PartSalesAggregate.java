package com.aspas.repository.mongo;

/**
 * Projection for part-level sales aggregation (must be top-level for Spring Data MongoDB).
 */
public interface PartSalesAggregate {
    String getId();             // partNumber
    /** Mongo $sum often maps as Long */
    Number getTotalQty();       // sum of quantitySold
    Double getTotalRevenue();   // sum of revenueAmount
}
