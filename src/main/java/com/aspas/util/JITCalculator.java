package com.aspas.util;

import java.util.List;

/**
 * ================================================================
 * JITCalculator — Just-In-Time Threshold Utility
 * ================================================================
 *
 * UML Traceability:
 *   - Activity Diagram: "Calculate Average Weekly Sales"
 *   - DFD Process: P2.0 Calculate JIT Thresholds
 *   - Business Rule:
 *       thresholdValue = total sales of last 7 days
 *       (Maintains exactly 1 week of stock buffer)
 *
 * The JIT philosophy:
 *   - Maintain ENOUGH parts to sustain sales for exactly ONE WEEK
 *   - Threshold = sum of all units sold in the last 7 days
 *   - If currentQuantity < threshold → reorder
 *   - Reorder quantity = threshold - currentQuantity
 *
 * ================================================================
 */
public class JITCalculator {

    /**
     * Calculate the JIT threshold for a part based on 7-day sales.
     *
     * UML Traceability:
     *   Activity Diagram → "Calculate Average Weekly Sales"
     *   Activity Diagram → "Set Threshold Value = Average Weekly Sales"
     *
     * Business Rule:
     *   threshold = total quantity sold in last 7 days
     *   This ensures stock covers exactly 1 week of demand.
     *
     * @param totalSoldInLast7Days sum of all units sold in the last 7 days
     * @return calculated threshold value
     */
    public static int calculateThreshold(int totalSoldInLast7Days) {
        // Threshold = total sold in 7 days (1 week buffer)
        // Minimum threshold of 1 to prevent parts from having 0 threshold
        return Math.max(totalSoldInLast7Days, 1);
    }

    /**
     * Calculate the reorder quantity needed to restore stock to threshold.
     *
     * UML Traceability:
     *   Sequence Diagram → Inside opt block (Message #21)
     *   "Calculate amount needed (1-week buffer)"
     *
     * Formula: reorderQty = thresholdValue - currentQuantity
     *
     * @param thresholdValue JIT threshold (1-week demand)
     * @param currentQuantity current stock count
     * @return quantity to order (0 if no reorder needed)
     */
    public static int calculateReorderQuantity(int thresholdValue, int currentQuantity) {
        int needed = thresholdValue - currentQuantity;
        return Math.max(needed, 0);
    }

    /**
     * Calculate threshold from a list of daily quantities sold.
     *
     * @param dailyQuantities list of quantities sold each day (up to 7 days)
     * @return calculated threshold
     */
    public static int calculateThresholdFromDailyData(List<Integer> dailyQuantities) {
        if (dailyQuantities == null || dailyQuantities.isEmpty()) {
            return 1; // Minimum threshold
        }

        int totalSold = dailyQuantities.stream()
            .mapToInt(Integer::intValue)
            .sum();

        return calculateThreshold(totalSold);
    }

    /**
     * Determine if a part needs reordering.
     *
     * @param currentQuantity current stock
     * @param thresholdValue JIT threshold
     * @return true if reorder needed
     */
    public static boolean needsReorder(int currentQuantity, int thresholdValue) {
        return currentQuantity < thresholdValue;
    }
}