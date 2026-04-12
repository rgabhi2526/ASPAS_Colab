package com.aspas.repository.jpa;

import com.aspas.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByPartNumber(String partNumber);

    List<OrderItem> findByVendorId(Long vendorId);

    @Query("SELECT COALESCE(SUM(oi.requiredQuantity), 0) FROM OrderItem oi " +
           "WHERE oi.partNumber = :partNumber")
    Integer getTotalOrderedQuantity(@Param("partNumber") String partNumber);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId AND oi.vendorId = :vendorId")
    List<OrderItem> findByOrderAndVendor(
        @Param("orderId") Long orderId,
        @Param("vendorId") Long vendorId
    );
}