package com.aspas.repository.mongo;

/**
 * Aggregation result DTO for part-level sales totals.
 *
 * Must be a concrete class (not an interface) — Spring Data MongoDB 4.2.x
 * cannot resolve getDomainType() for interface projections on @Aggregation
 * return types, causing a NullPointerException in isSimpleReturnType().
 */
public class PartSalesAggregate {

    private String id;           // _id = partNumber from $group
    private Long totalQty;       // $sum of quantitySold
    private Double totalRevenue; // $sum of revenueAmount

    public PartSalesAggregate() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getTotalQty() { return totalQty; }
    public void setTotalQty(Long totalQty) { this.totalQty = totalQty; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
}
