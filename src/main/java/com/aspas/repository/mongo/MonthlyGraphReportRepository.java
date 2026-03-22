package com.aspas.repository.mongo;

import com.aspas.model.document.MonthlyGraphReportDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * ================================================================
 * MonthlyGraphReportRepository — MongoDB
 * ================================================================
 *
 * UML Traceability:
 *   - Class Diagram  : MonthlyGraphReport (extends Report)
 *   - Database       : monthly_graph_reports collection (MongoDB)
 *   - Use Case       : UC-08 View Monthly Sales Graph
 *   - Sequence Diagram: Message #26 "<<create>> MonthlyGraphReport"
 *
 * Stores pre-computed monthly reports with daily data points.
 * Each document = one month's revenue data ready for graphing.
 *
 * ================================================================
 */
@Repository
public interface MonthlyGraphReportRepository extends MongoRepository<MonthlyGraphReportDoc, String> {

    /**
     * Find a monthly report by its business key.
     *
     * @param reportId e.g., "RPT-MONTH-202603"
     * @return matching report
     */
    Optional<MonthlyGraphReportDoc> findByReportId(String reportId);

    /**
     * Find a monthly report by month and year.
     *
     * UML Traceability:
     *   REST API: GET /api/reports/monthly/{year}/{month}
     *
     * @param targetMonth month (1-12)
     * @param targetYear year (e.g., 2026)
     * @return matching report
     */
    Optional<MonthlyGraphReportDoc> findByTargetMonthAndTargetYear(
        Integer targetMonth,
        Integer targetYear
    );

    /**
     * Check if a report already exists for a given month/year.
     *
     * @param targetMonth month
     * @param targetYear year
     * @return true if report exists
     */
    boolean existsByTargetMonthAndTargetYear(Integer targetMonth, Integer targetYear);

    /**
     * Find all reports for a specific year.
     *
     * @param targetYear year
     * @return all monthly reports for that year
     */
    List<MonthlyGraphReportDoc> findByTargetYear(Integer targetYear);

    /**
     * Find all reports sorted by year and month descending.
     *
     * @return all monthly reports, newest first
     */
    List<MonthlyGraphReportDoc> findAllByOrderByTargetYearDescTargetMonthDesc();
}