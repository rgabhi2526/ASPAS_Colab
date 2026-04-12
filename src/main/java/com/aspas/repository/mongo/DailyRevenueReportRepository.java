package com.aspas.repository.mongo;

import com.aspas.model.document.DailyRevenueReportDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyRevenueReportRepository extends MongoRepository<DailyRevenueReportDoc, String> {

    Optional<DailyRevenueReportDoc> findByReportId(String reportId);

    Optional<DailyRevenueReportDoc> findByReportDate(LocalDate reportDate);

    boolean existsByReportDate(LocalDate reportDate);

    @Query("{ 'reportDate': { $gte: ?0, $lte: ?1 } }")
    List<DailyRevenueReportDoc> findByDateRange(LocalDate startDate, LocalDate endDate);

    List<DailyRevenueReportDoc> findAllByOrderByReportDateDesc();
}