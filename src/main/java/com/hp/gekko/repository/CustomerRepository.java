package com.hp.gekko.repository;

import com.hp.gekko.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Customer Repository - Data Access Layer for Customer Entity
 * 
 * This repository provides CRUD operations and custom queries for Customer entity.
 * It extends JpaRepository which provides basic CRUD methods.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customer by email address
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find customer by API Key ID - Used for authentication via APIGEE
     */
    Optional<Customer> findByApiKeyId(String apiKeyId);

    /**
     * Find all customers by Business Unit (BU) name
     * Used for multi-tenancy data isolation
     */
    List<Customer> findByBuName(String buName);

    /**
     * Find all active customers
     */
    @Query("SELECT c FROM Customer c WHERE c.status = 'ACTIVE' AND c.deletedAt IS NULL")
    List<Customer> findAllActiveCustomers();

    /**
     * Find customer by API Key and Secret (for authentication)
     */
    @Query("SELECT c FROM Customer c WHERE c.apiKeyId = :apiKeyId AND c.apiSecretKey = :apiSecretKey")
    Optional<Customer> findByApiCredentials(
        @Param("apiKeyId") String apiKeyId,
        @Param("apiSecretKey") String apiSecretKey
    );

    boolean existsByEmail(String email);

    List<Customer> findByCountry(String country);
}
