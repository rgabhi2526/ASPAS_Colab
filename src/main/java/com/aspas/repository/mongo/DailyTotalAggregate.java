package com.aspas.repository.mongo;

/**
 * Projection for single-day total aggregation. Top-level for Spring Data MongoDB.
 */
public interface DailyTotalAggregate {
    Double getTotalRevenue();       // day's total revenue
    /** Mongo $sum: 1 often maps as Long — use Number for compatibility */
    Number getTotalTransactions();
}
