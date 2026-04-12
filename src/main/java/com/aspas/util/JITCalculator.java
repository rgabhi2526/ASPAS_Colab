package com.aspas.util;

import java.util.List;

public class JITCalculator {

    public static int calculateThreshold(int totalSoldInLast7Days) {

        return Math.max(totalSoldInLast7Days, 1);
    }

    public static int calculateReorderQuantity(int thresholdValue, int currentQuantity) {
        int needed = thresholdValue - currentQuantity;
        return Math.max(needed, 0);
    }

    public static int calculateThresholdFromDailyData(List<Integer> dailyQuantities) {
        if (dailyQuantities == null || dailyQuantities.isEmpty()) {
            return 1; 
        }

        int totalSold = dailyQuantities.stream()
            .mapToInt(Integer::intValue)
            .sum();

        return calculateThreshold(totalSold);
    }

    public static boolean needsReorder(int currentQuantity, int thresholdValue) {
        return currentQuantity < thresholdValue;
    }
}