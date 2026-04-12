package com.aspas.service;

import com.aspas.model.entity.SparePart;
import com.aspas.repository.jpa.SparePartRepository;
import com.aspas.repository.mongo.SalesTransactionRepository;
import com.aspas.repository.mongo.SalesTransactionRepository.PartSalesAggregate;
import com.aspas.util.JITCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JITService {

    private final SparePartRepository sparePartRepository;
    private final SalesTransactionRepository salesTransactionRepository;

    @Transactional
    public int calculateJITThresholds() {

        log.info("═══ PROCESS 2.0: Starting JIT Threshold Calculation ═══");

        List<SparePart> allParts = sparePartRepository.findAll();
        log.info("  Total parts to process: {}", allParts.size());

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7)
            .with(LocalTime.MIN);

        log.debug("  7-day window: {} to {}", startDate, endDate);

        int updatedCount = 0;

        for (SparePart part : allParts) {

            int totalSoldIn7Days = getTotalSoldInPeriod(
                part.getPartNumber(), startDate, endDate
            );

            int newThreshold = JITCalculator.calculateThreshold(totalSoldIn7Days);

            int oldThreshold = part.getThresholdValue();

            part.updateThreshold(newThreshold);
            sparePartRepository.save(part);

            updatedCount++;

            log.debug("  Part {} [{}]: 7-day sales = {}, Threshold: {} → {}",
                part.getPartNumber(), part.getPartName(),
                totalSoldIn7Days, oldThreshold, newThreshold);
        }

        log.info("═══ PROCESS 2.0 COMPLETE: {} thresholds updated ═══", updatedCount);

        return updatedCount;
    }

    public int calculateThresholdForPart(String partNumber) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7).with(LocalTime.MIN);

        int totalSold = getTotalSoldInPeriod(partNumber, startDate, endDate);
        return JITCalculator.calculateThreshold(totalSold);
    }

    private int getTotalSoldInPeriod(
            String partNumber,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<PartSalesAggregate> result =
            salesTransactionRepository.aggregateSalesForPart(
                partNumber, startDate, endDate
            );

        if (result != null && !result.isEmpty()) {
            Integer totalQty = result.get(0).getTotalQty();
            return totalQty != null ? totalQty : 0;
        }

        return 0;
    }

    public List<SparePart> getAllThresholds() {
        return sparePartRepository.findAll();
    }
}