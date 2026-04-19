package com.aspas.util;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JITCalculatorTest {

    // ---------------------------------------------------------
    // Testing: calculateThreshold()
    // ---------------------------------------------------------
    @Test
    void testCalculateThreshold_NormalSales() {
        // If we sold 25 items, the threshold should be 25
        int result = JITCalculator.calculateThreshold(25);
        assertEquals(25, result, "Threshold should equal total sales");
    }

    @Test
    void testCalculateThreshold_ZeroSales_DefaultsToOne() {
        // Business Rule: Threshold should never be less than 1
        int result = JITCalculator.calculateThreshold(0);
        assertEquals(1, result, "If 0 items were sold, threshold must default to 1");
    }

    // ---------------------------------------------------------
    // Testing: calculateReorderQuantity()
    // ---------------------------------------------------------
    @Test
    void testCalculateReorderQuantity_NeedsRestock() {
        // If threshold is 10, and we only have 3, we need to order 7
        int result = JITCalculator.calculateReorderQuantity(10, 3);
        assertEquals(7, result, "Should order the exact difference to reach threshold");
    }

    @Test
    void testCalculateReorderQuantity_AlreadyOverstocked() {
        // If threshold is 10, but we have 15, we should order 0
        int result = JITCalculator.calculateReorderQuantity(10, 15);
        assertEquals(0, result, "Should not order negative amounts if overstocked");
    }

    // ---------------------------------------------------------
    // Testing: calculateThresholdFromDailyData()
    // ---------------------------------------------------------
    @Test
    void testCalculateThresholdFromDailyData_NullList() {
        // Null lists should safely return 1
        int result = JITCalculator.calculateThresholdFromDailyData(null);
        assertEquals(1, result, "Null list should default to threshold of 1");
    }

    @Test
    void testCalculateThresholdFromDailyData_ValidData() {
        // 2 + 3 + 5 = 10 total sales
        List<Integer> salesData = Arrays.asList(2, 3, 5);
        int result = JITCalculator.calculateThresholdFromDailyData(salesData);
        assertEquals(10, result, "Should sum all daily sales correctly");
    }

    // ---------------------------------------------------------
    // Testing: needsReorder()
    // ---------------------------------------------------------
    @Test
    void testNeedsReorder_WhenBelowThreshold() {
        // Current stock (4) is less than threshold (10)
        boolean result = JITCalculator.needsReorder(4, 10);
        assertTrue(result, "Should return true when stock is below threshold");
    }

    @Test
    void testNeedsReorder_WhenAboveThreshold() {
        // Current stock (12) is more than threshold (10)
        boolean result = JITCalculator.needsReorder(12, 10);
        assertFalse(result, "Should return false when stock is sufficient");
    }
}