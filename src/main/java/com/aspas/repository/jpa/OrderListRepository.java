package com.aspas.repository.jpa;

import com.aspas.model.entity.OrderList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ================================================================
 * OrderListRepository — JPA (MySQL)
 * ================================================================
 *
 * UML Traceability:
 *   - Class Diagram     : OrderList class (Composition parent)
 *   - Sequence Diagram  : Message #15 "<<create>> OrderList"
 *   - Use Case          : UC-03 Generate Daily Orders
 *
 * Provides:
 *   - Standard CRUD via JpaRepository
 *   - Find orders by date
 *   - Find unprinted orders
 *   - Date range queries
 *
 * Note: OrderItems are fetched EAGERLY via the @OneToMany
 * relationship defined in OrderList entity (Composition).
 *
 * ================================================================
 */
@Repository
public interface OrderListRepository extends JpaRepository<OrderList, Long> {

    /**
     * Find order list generated on a specific date.
     *
     * UML Traceability:
     *   End-of-day process generates one order list per day.
     *
     * @param orderDate date of the order
     * @return Optional containing the order if found
     */
    Optional<OrderList> findByOrderDate(LocalDate orderDate);

    /**
     * Check if an order already exists for a given date.
     * Prevents duplicate order generation.
     *
     * @param orderDate date to check
     * @return true if order exists
     */
    boolean existsByOrderDate(LocalDate orderDate);

    /**
     * Find all orders within a date range.
     *
     * @param startDate range start (inclusive)
     * @param endDate range end (inclusive)
     * @return orders in the range
     */
    @Query("SELECT ol FROM OrderList ol WHERE ol.orderDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ol.orderDate DESC")
    List<OrderList> findByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find all orders that have not been printed yet.
     *
     * @return unprinted orders
     */
    List<OrderList> findByIsPrintedFalse();

    /**
     * Find all orders sorted by date descending (newest first).
     *
     * @return all orders sorted
     */
    List<OrderList> findAllByOrderByOrderDateDesc();

    /**
     * Find orders with at least one item (non-empty orders).
     *
     * @return orders that have items to order
     */
    @Query("SELECT ol FROM OrderList ol WHERE ol.totalItems > 0 ORDER BY ol.orderDate DESC")
    List<OrderList> findNonEmptyOrders();
}