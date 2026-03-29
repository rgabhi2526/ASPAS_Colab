package com.aspas.repository.jpa;

import com.aspas.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * ================================================================
 * OrderItemRepository — JPA (MySQL)
 * ================================================================
 *
 * UML Traceability:
 *   - Class Diagram     : OrderItem class (Composition child)
 *   - Sequence Diagram  : Message #21 "<<create>> OrderItem"
 *   - Relationship      : OrderList 1 *── OrderItem (Composition)
 *
 * Provides:
 *   - Standard CRUD via JpaRepository
 *   - Find items by order ID
 *   - Find items by part or vendor
 *
 * Note: OrderItems are typically accessed through their parent
 * OrderList entity (via Composition). Direct repository access
 * is provided for reporting and administrative queries.
 *
 * ================================================================
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all items belonging to a specific order.
     *
     * @param orderId parent order list ID
     * @return order items for that order
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Find all order items for a specific part (across all orders).
     * Useful for tracking order history of a part.
     *
     * @param partNumber part number
     * @return order items containing this part
     */
    List<OrderItem> findByPartNumber(String partNumber);

    /**
     * Find all order items directed to a specific vendor (across all orders).
     *
     * @param vendorId vendor ID
     * @return order items for that vendor
     */
    List<OrderItem> findByVendorId(Long vendorId);

    /**
     * Get total quantity ordered for a specific part across all orders.
     *
     * @param partNumber part number
     * @return total quantity ordered historically
     */
    @Query("SELECT COALESCE(SUM(oi.requiredQuantity), 0) FROM OrderItem oi " +
           "WHERE oi.partNumber = :partNumber")
    Integer getTotalOrderedQuantity(@Param("partNumber") String partNumber);

    /**
     * Find items grouped by vendor for a specific order.
     * Useful for splitting an order list by vendor for separate dispatch.
     *
     * @param orderId order ID
     * @param vendorId vendor ID
     * @return items for that order-vendor combination
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId AND oi.vendorId = :vendorId")
    List<OrderItem> findByOrderAndVendor(
        @Param("orderId") Long orderId,
        @Param("vendorId") Long vendorId
    );
}