package com.aspas.repository.mongo;

/**
 * Projection for daily revenue aggregation (monthly graph). Top-level for Spring Data MongoDB.
 */
public interface DailyRevenueAggregate {
    Number getId();                 // day of month (1-31)
    Double getDailyRevenue();       // total revenue for that day
    Number getTransactionCount();   // number of transactions
}
