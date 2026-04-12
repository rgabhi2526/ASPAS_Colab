package com.aspas.repository.jpa;

import com.aspas.model.entity.OrderList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderListRepository extends JpaRepository<OrderList, Long> {

    Optional<OrderList> findByOrderDate(LocalDate orderDate);

    boolean existsByOrderDate(LocalDate orderDate);

    @Query("SELECT ol FROM OrderList ol WHERE ol.orderDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ol.orderDate DESC")
    List<OrderList> findByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    List<OrderList> findByIsPrintedFalse();

    List<OrderList> findAllByOrderByOrderDateDesc();

    @Query("SELECT ol FROM OrderList ol WHERE ol.totalItems > 0 ORDER BY ol.orderDate DESC")
    List<OrderList> findNonEmptyOrders();
}