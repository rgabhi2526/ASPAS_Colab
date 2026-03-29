package com.aspas.repository.mongo;

import com.aspas.model.document.DailyRevenueReportDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ================================================================
 * DailyRevenueReportRepository — MongoDB
 * ================================================================
 *
 * UML Traceability:
 *   - Class Diagram  : DailyRevenueReport (extends Report)
 *   - Database       : daily_revenue_reports collection (MongoDB)
 *   - Use Case       : UC-07 View Daily Revenue Log
 *   - Sequence Diagram: Message #27 "<<create>> DailyRevenueReport"
 *
 * Stores pre-computed daily revenue summaries.
 * Each document = one day's aggregated sales data.
 *
 * ================================================================
 */
@Repository
public interface DailyRevenueReportRepository extends MongoRepository<DailyRevenueReportDoc, String> {

    /**
     * Find a daily report by its business key.
     *
     * @param reportId e.g., "RPT-DAILY-20260315"
     * @return matching report
     */
    Optional<DailyRevenueReportDoc> findByReportId(String reportId);

    /**
     * Find a daily report by date.
     *
     * UML Traceability:
     *   REST API: GET /api/reports/daily/{date}
     *
     * @param reportDate date of the report
     * @return matching report
     */
    Optional<DailyRevenueReportDoc> findByReportDate(LocalDate reportDate);

    /**
     * Check if a report already exists for a given date.
     *
     * @param reportDate date to check
     * @return true if report exists
     */
    boolean existsByReportDate(LocalDate reportDate);

    /**
     * Find reports within a date range.
     *
     * @param startDate range start
     * @param endDate range end
     * @return reports in range
     */
    @Query("{ 'reportDate': { $gte: ?0, $lte: ?1 } }")
    List<DailyRevenueReportDoc> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Find all reports sorted by date descending.
     *
     * @return all daily reports, newest first
     */
    List<DailyRevenueReportDoc> findAllByOrderByReportDateDesc();
}