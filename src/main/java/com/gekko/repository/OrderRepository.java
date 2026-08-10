package com.gekko.repository;

import com.gekko.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for Orders. We expose basic lookup operations.
 */
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByExternalId(String externalId);
}
