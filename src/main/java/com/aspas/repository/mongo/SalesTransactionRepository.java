package com.aspas.repository.mongo;

import com.aspas.model.document.SalesTransactionDoc;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ================================================================
 * SalesTransactionRepository — MongoDB
 * ================================================================
 *
 * UML Traceability:
 *   - Class Diagram  : SalesTransaction class
 *   - DFD Store      : D2 Sales Log
 *   - Sequence Diagram:
 *       Message #7     : logSale()     → save()
 *       Message #12-13 : getTxnData()  → findByPartAndDateRange()
 *       Message #28    : report reads  → aggregation queries
 *   - Use Cases:
 *       UC-01 : Process Sale (write)
 *       UC-02 : Calculate JIT Thresholds (read 7-day history)
 *       UC-07 : Daily Revenue Report (aggregate by date)
 *       UC-08 : Monthly Graph Report (aggregate by month)
 *
 * This is the MOST query-intensive repository.
 * MongoDB's aggregation pipeline is used for:
 *   - 7-day rolling sales for JIT
 *   - Daily revenue totals
 *   - Monthly revenue grouped by day
 *
 * ================================================================
 */
@Repository
public interface SalesTransactionRepository extends MongoRepository<SalesTransactionDoc, String> {

    // ══════════════════════════════════════════
    //  BASIC LOOKUPS
    // ══════════════════════════════════════════

    /**
     * Find a transaction by its business key.
     *
     * @param transactionId business key (e.g., "TXN-20260315-001")
     * @return matching transaction
     */
    SalesTransactionDoc findByTransactionId(String transactionId);

    /**
     * Find all transactions for a specific part.
     *
     * @param partNumber part identifier
     * @return all sales of that part
     */
    List<SalesTransactionDoc> findByPartNumber(String partNumber);


    // ══════════════════════════════════════════
    //  JIT THRESHOLD CALCULATION QUERIES
    // ══════════════════════════════════════════

    /**
     * Find all sales of a specific part within a date range.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #12-13
     *   DFD: P2.0 reads from D2 Sales Log
     *   Activity Diagram: "Get Sales Data for Last 7 Days"
     *
     * Primary use: JIT threshold calculation
     *   → Get all sales of part X in the last 7 days
     *   → Sum quantities → compute average → set as threshold
     *
     * @param partNumber part to query
     * @param startDate beginning of range
     * @param endDate end of range
     * @return transactions in range
     */
    @Query("{ 'partNumber': ?0, 'transactionDate': { $gte: ?1, $lte: ?2 } }")
    List<SalesTransactionDoc> findByPartNumberAndDateRange(
        String partNumber,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Find all transactions within a date range (all parts).
     *
     * @param startDate beginning of range
     * @param endDate end of range
     * @return all transactions in range
     */
    @Query("{ 'transactionDate': { $gte: ?0, $lte: ?1 } }")
    List<SalesTransactionDoc> findByDateRange(
        LocalDateTime startDate,
        LocalDateTime endDate
    );


    // ══════════════════════════════════════════
    //  DAILY REVENUE QUERIES
    // ══════════════════════════════════════════

    /**
     * Find all transactions on a specific date.
     *
     * UML Traceability:
     *   Use Case: UC-07 View Daily Revenue Log
     *   Sequence Diagram → Message #28 "R → ST : reads data"
     *
     * @param startOfDay start of the day (00:00:00)
     * @param endOfDay end of the day (23:59:59)
     * @return all transactions on that day
     */
    @Query("{ 'transactionDate': { $gte: ?0, $lte: ?1 } }")
    List<SalesTransactionDoc> findTransactionsForDay(
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );


    // ══════════════════════════════════════════
    //  AGGREGATION QUERIES (MongoDB Pipeline)
    // ══════════════════════════════════════════

    /**
     * Aggregate total quantity sold for a specific part in a date range.
     *
     * Used by JITService to calculate:
     *   threshold = total quantity sold in last 7 days
     *
     * UML Traceability:
     *   Activity Diagram: "Calculate Average Weekly Sales"
     *
     * @param partNumber part to aggregate
     * @param startDate range start
     * @param endDate range end
     * @return list with one result containing totalQty
     */
    @Aggregation(pipeline = {
        "{ $match: { 'partNumber': ?0, 'transactionDate': { $gte: ?1, $lte: ?2 } } }",
        "{ $group: { '_id': '$partNumber', 'totalQty': { $sum: '$quantitySold' }, 'totalRevenue': { $sum: '$revenueAmount' } } }"
    })
    List<PartSalesAggregate> aggregateSalesForPart(
        String partNumber,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Aggregate daily revenue totals for a given month.
     *
     * Used by ReportService to generate monthly graph data points.
     *
     * UML Traceability:
     *   Use Case: UC-08 View Monthly Sales Graph
     *   Sequence Diagram → Message #28 "R → ST : reads data"
     *
     * Returns one document per day with the revenue total.
     *
     * @param startDate first day of month (00:00:00)
     * @param endDate last day of month (23:59:59)
     * @return daily revenue totals
     */
    @Aggregation(pipeline = {
        "{ $match: { 'transactionDate': { $gte: ?0, $lte: ?1 } } }",
        "{ $group: { '_id': { $dayOfMonth: '$transactionDate' }, 'dailyRevenue': { $sum: '$revenueAmount' }, 'transactionCount': { $sum: 1 } } }",
        "{ $sort: { '_id': 1 } }"
    })
    List<DailyRevenueAggregate> aggregateDailyRevenueForMonth(
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Aggregate total revenue for a specific day.
     *
     * UML Traceability:
     *   Use Case: UC-07 View Daily Revenue Log
     *
     * @param startOfDay start of day
     * @param endOfDay end of day
     * @return daily total
     */
    @Aggregation(pipeline = {
        "{ $match: { 'transactionDate': { $gte: ?0, $lte: ?1 } } }",
        "{ $group: { '_id': null, 'totalRevenue': { $sum: '$revenueAmount' }, 'totalTransactions': { $sum: 1 } } }"
    })
    List<DailyTotalAggregate> aggregateDailyTotal(
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );

    /**
     * Aggregate top selling parts for a given day.
     *
     * @param startOfDay start of day
     * @param endOfDay end of day
     * @return top parts sorted by quantity sold descending
     */
    @Aggregation(pipeline = {
        "{ $match: { 'transactionDate': { $gte: ?0, $lte: ?1 } } }",
        "{ $group: { '_id': '$partNumber', 'partName': { $first: '$partName' }, 'totalQty': { $sum: '$quantitySold' }, 'totalRevenue': { $sum: '$revenueAmount' } } }",
        "{ $sort: { 'totalQty': -1 } }",
        "{ $limit: 10 }"
    })
    List<TopSellingAggregate> aggregateTopSellingParts(
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );


    // ══════════════════════════════════════════
    //  COUNT QUERIES
    // ══════════════════════════════════════════

    /**
     * Count transactions for a specific part.
     *
     * @param partNumber part identifier
     * @return count
     */
    long countByPartNumber(String partNumber);

    /**
     * Count transactions in a date range.
     *
     * @param startDate range start
     * @param endDate range end
     * @return count
     */
    @Query(value = "{ 'transactionDate': { $gte: ?0, $lte: ?1 } }", count = true)
    long countByDateRange(LocalDateTime startDate, LocalDateTime endDate);


    // ══════════════════════════════════════════
    //  AGGREGATION RESULT INTERFACES
    //  (Spring Data MongoDB projects results
    //   into these interfaces automatically)
    // ══════════════════════════════════════════

    /**
     * Projection for part-level sales aggregation.
     * Used by JIT calculation.
     */
    interface PartSalesAggregate {
        String getId();             // partNumber
        Integer getTotalQty();      // sum of quantitySold
        Double getTotalRevenue();   // sum of revenueAmount
    }

    /**
     * Projection for daily revenue aggregation.
     * Used by monthly graph report.
     */
    interface DailyRevenueAggregate {
        Integer getId();                // day of month (1-31)
        Double getDailyRevenue();       // total revenue for that day
        Integer getTransactionCount();  // number of transactions
    }

    /**
     * Projection for single-day total aggregation.
     * Used by daily revenue report.
     */
    interface DailyTotalAggregate {
        Double getTotalRevenue();       // day's total revenue
        Integer getTotalTransactions(); // day's total transactions
    }

    /**
     * Projection for top-selling parts aggregation.
     */
    interface TopSellingAggregate {
        String getId();             // partNumber
        String getPartName();       // part name
        Integer getTotalQty();      // total quantity sold
        Double getTotalRevenue();   // total revenue from this part
    }
}