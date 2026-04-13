package com.aspas.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aggregated sales for one calendar day from {@code sales_transactions} (MongoDB).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesDayStatsDTO {

    private Double totalRevenue;
    private Integer transactionCount;
}
