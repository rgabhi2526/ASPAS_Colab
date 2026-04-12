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

/**
 * ================================================================
 * JITService — DFD Process 2.0: Calculate JIT Thresholds
 * ================================================================
 *
 * UML Traceability:
 *   - DFD Process: P2.0 Calculate JIT Thresholds
 *   - Activity Diagram: JIT Logic Execution (complete loop)
 *   - Sequence Diagram: Messages #10 through #14
 *   - Use Case: UC-02 Calculate JIT Thresholds
 *   - Class Diagram: SystemController.calculateJITThresholds()
 *
 * Activity Diagram Implementation:
 *   Start → Fetch All Spare Parts
 *     → Loop: More Parts?
 *       → YES: Get Sales Data for Last 7 Days
 *         → Calculate Average Weekly Sales
 *         → Set Threshold = Total Weekly Sales
 *         → Update Inventory Database
 *         → (back to loop)
 *       → NO: End Process
 *
 * Business Rule:
 *   thresholdValue = total quantity sold in last 7 days
 *   This ensures stock buffer of exactly 1 week.
 *
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JITService {

    private final SparePartRepository sparePartRepository;
    private final SalesTransactionRepository salesTransactionRepository;

    /**
     * Calculate and update JIT thresholds for ALL spare parts.
     *
     * This method implements the COMPLETE Activity Diagram:
     *   Start → Fetch All Parts → Loop → Calculate → Update → End
     *
     * And Sequence Diagram Messages #10-14:
     *   Msg #10-11: SC → SC : calculateJITThresholds() (self-call)
     *   Msg #12-13: SC → ST : getTransactionDetails() (read 7-day sales)
     *   Msg #14   : SC → SP : updateThreshold(newThreshold)
     *
     * @return number of parts whose thresholds were updated
     */
    @Transactional
    public int calculateJITThresholds() {

        log.info("═══ PROCESS 2.0: Starting JIT Threshold Calculation ═══");

        // ─────────────────────────────────────────────
        // ACTIVITY DIAGRAM: "Retrieve All Spare Parts"
        // ─────────────────────────────────────────────
        List<SparePart> allParts = sparePartRepository.findAll();
        log.info("  Total parts to process: {}", allParts.size());

        // Define the 7-day lookback window
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7)
            .with(LocalTime.MIN);

        log.debug("  7-day window: {} to {}", startDate, endDate);

        int updatedCount = 0;

        // ─────────────────────────────────────────────
        // ACTIVITY DIAGRAM: "More Parts to Process?" → LOOP
        // ─────────────────────────────────────────────
        for (SparePart part : allParts) {
            if (part == null || part.getPartNumber() == null || part.getPartNumber().isBlank()) {
                log.warn("Skipping invalid spare part row (null or blank partNumber)");
                continue;
            }

            // ─────────────────────────────────────────
            // ACTIVITY DIAGRAM: "Get Sales Data for Last 7 Days"
            // SEQUENCE DIAGRAM: Message #12-13
            //   SC → ST : getTransactionDetails()
            //   Read from D2 Sales Log (MongoDB)
            // ─────────────────────────────────────────
            int totalSoldIn7Days = getTotalSoldInPeriod(
                part.getPartNumber(), startDate, endDate
            );

            // ─────────────────────────────────────────
            // ACTIVITY DIAGRAM: "Calculate Average Weekly Sales"
            // ACTIVITY DIAGRAM: "Set Threshold = Average Weekly Sales"
            // ─────────────────────────────────────────
            int newThreshold = JITCalculator.calculateThreshold(totalSoldIn7Days);

            int oldThreshold = part.getThresholdValue() != null ? part.getThresholdValue() : 0;

            // ─────────────────────────────────────────
            // ACTIVITY DIAGRAM: "Update Inventory Database"
            // SEQUENCE DIAGRAM: Message #14
            //   SC → SP : updateThreshold(newThreshold)
            //   Write to D1 Inventory File (MySQL)
            // ─────────────────────────────────────────
            part.updateThreshold(newThreshold);
            sparePartRepository.save(part);

            updatedCount++;

            log.debug("  Part {} [{}]: 7-day sales = {}, Threshold: {} → {}",
                part.getPartNumber(), part.getPartName() != null ? part.getPartName() : "",
                totalSoldIn7Days, oldThreshold, newThreshold);
        }

        log.info("═══ PROCESS 2.0 COMPLETE: {} thresholds updated ═══", updatedCount);

        return updatedCount;
    }

    /**
     * Calculate threshold for a SINGLE part.
     *
     * @param partNumber part to calculate for
     * @return the new threshold value
     */
    public int calculateThresholdForPart(String partNumber) {
        if (partNumber == null || partNumber.isBlank()) {
            return JITCalculator.calculateThreshold(0);
        }
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7).with(LocalTime.MIN);

        int totalSold = getTotalSoldInPeriod(partNumber, startDate, endDate);
        return JITCalculator.calculateThreshold(totalSold);
    }

    /**
     * Get total quantity sold for a part in a given period.
     *
     * Uses MongoDB aggregation pipeline to sum quantitySold.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #12-13
     *   DFD: P2.0 reads from D2 Sales Log
     *
     * @param partNumber part identifier
     * @param startDate period start
     * @param endDate period end
     * @return total units sold
     */
    private int getTotalSoldInPeriod(
            String partNumber,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        try {
            List<PartSalesAggregate> result =
                salesTransactionRepository.aggregateSalesForPart(
                    partNumber, startDate, endDate
                );

            if (result != null && !result.isEmpty()) {
                Integer totalQty = result.get(0).getTotalQty();
                return totalQty != null ? totalQty : 0;
            }
        } catch (Exception e) {
            log.warn("Mongo read failed for part {} (JIT uses 0 sold): {}", partNumber, e.getMessage());
        }

        return 0;
    }

    /**
     * Get all current threshold values.
     * Used by API endpoint: GET /api/jit/thresholds
     *
     * @return all parts with their threshold values
     */
    public List<SparePart> getAllThresholds() {
        return sparePartRepository.findAll();
    }
}