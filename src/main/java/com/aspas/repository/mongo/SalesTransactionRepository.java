package com.aspas.repository.mongo;

import com.aspas.model.document.SalesTransactionDoc;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesTransactionRepository extends MongoRepository<SalesTransactionDoc, String> {

    SalesTransactionDoc findByTransactionId(String transactionId);

    List<SalesTransactionDoc> findByPartNumber(String partNumber);

    @Query("{ 'partNumber': ?0, 'transactionDate': { $gte: ?1, $lte: ?2 } }")
    List<SalesTransactionDoc> findByPartNumberAndDateRange(
        String partNumber,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    @Query("{ 'transactionDate': { $gte: ?0, $lte: ?1 } }")
    List<SalesTransactionDoc> findByDateRange(
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    @Query("{ 'transactionDate': { $gte: ?0, $lte: ?1 } }")
    List<SalesTransactionDoc> findTransactionsForDay(
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );

    @Aggregation(pipeline = {
        "{ $match: { 'partNumber': ?0, 'transactionDate': { $gte: ?1, $lte: ?2 } } }",
        "{ $group: { '_id': '$partNumber', 'totalQty': { $sum: '$quantitySold' }, 'totalRevenue': { $sum: '$revenueAmount' } } }"
    })
    List<PartSalesAggregate> aggregateSalesForPart(
        String partNumber,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    @Aggregation(pipeline = {
        "{ $match: { 'transactionDate': { $gte: ?0, $lte: ?1 } } }",
        "{ $group: { '_id': { $dayOfMonth: '$transactionDate' }, 'dailyRevenue': { $sum: '$revenueAmount' }, 'transactionCount': { $sum: 1 } } }",
        "{ $sort: { '_id': 1 } }"
    })
    List<DailyRevenueAggregate> aggregateDailyRevenueForMonth(
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    @Aggregation(pipeline = {
        "{ $match: { 'transactionDate': { $gte: ?0, $lte: ?1 } } }",
        "{ $group: { '_id': null, 'totalRevenue': { $sum: '$revenueAmount' }, 'totalTransactions': { $sum: 1 } } }"
    })
    List<DailyTotalAggregate> aggregateDailyTotal(
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );

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

    long countByPartNumber(String partNumber);

    @Query(value = "{ 'transactionDate': { $gte: ?0, $lte: ?1 } }", count = true)
    long countByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    interface PartSalesAggregate {
        String getId();             
        Integer getTotalQty();      
        Double getTotalRevenue();   
    }

    interface DailyRevenueAggregate {
        Integer getId();                
        Double getDailyRevenue();       
        Integer getTransactionCount();  
    }

    interface DailyTotalAggregate {
        Double getTotalRevenue();       
        Integer getTotalTransactions(); 
    }

    interface TopSellingAggregate {
        String getId();             
        String getPartName();       
        Integer getTotalQty();      
        Double getTotalRevenue();   
    }
}