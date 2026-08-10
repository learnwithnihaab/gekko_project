package com.hp.gekko.repository;

import com.hp.gekko.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Repository - Data Access Layer for Order Entity
 * Provides CRUD operations and custom queries for managing orders.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findBySalesOrderNumber(String salesOrderNumber);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByOrderStatus(String orderStatus);

    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'SUBSCRIPTION_CREATED'")
    List<Order> findPendingOrdersForBrim();

    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'SENT_TO_BRIM' AND o.sentToBrimAt < :timestamp")
    List<Order> findOrdersWaitingForBrimResponse(@Param("timestamp") LocalDateTime sentBefore);

    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'FAILED' AND o.retryCount < :maxRetries")
    List<Order> findFailedOrdersForRetry(@Param("maxRetries") Integer maxRetries);

    @Query("SELECT o FROM Order o WHERE o.createdAt > :createdAfter ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(@Param("createdAfter") LocalDateTime createdAfter);

    List<Order> findByBrimStatus(String brimStatus);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status")
    Long countByOrderStatus(@Param("status") String orderStatus);
}
