package com.aspas.repository.mongo;

import com.aspas.model.document.MonthlyGraphReportDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyGraphReportRepository extends MongoRepository<MonthlyGraphReportDoc, String> {

    Optional<MonthlyGraphReportDoc> findByReportId(String reportId);

    Optional<MonthlyGraphReportDoc> findByTargetMonthAndTargetYear(
        Integer targetMonth,
        Integer targetYear
    );

    boolean existsByTargetMonthAndTargetYear(Integer targetMonth, Integer targetYear);

    List<MonthlyGraphReportDoc> findByTargetYear(Integer targetYear);

    List<MonthlyGraphReportDoc> findAllByOrderByTargetYearDescTargetMonthDesc();
}