package com.gekko.repository;

import com.gekko.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for Customer entity.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByExternalId(String externalId);
}
