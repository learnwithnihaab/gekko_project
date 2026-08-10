package com.gekko.repository;

import com.gekko.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByExternalId(String externalId);
}
