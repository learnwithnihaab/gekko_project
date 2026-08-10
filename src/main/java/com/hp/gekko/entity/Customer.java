package com.hp.gekko.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Customer Entity - Represents customer information stored in CIS (Customer Information System)
 * 
 * This entity stores customer data from upstream systems (Store, Argon, etc.)
 * who place orders for software subscriptions.
 * 
 * Database Table: customers
 * Mapped to: CIS (Customer Information System)
 */
@Entity
@Table(name = "customers")
@Data                   // Lombok: auto-generates getters, setters, equals, hashCode, toString
@NoArgsConstructor      // Lombok: auto-generates no-arg constructor
@AllArgsConstructor     // Lombok: auto-generates all-arg constructor
public class Customer {

    /**
     * Primary Key - Unique customer identifier (UUID)
     * Generated automatically by the database
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    /**
     * Customer's unique email address
     * Used for communication and authentication
     * Must be unique across the system
     */
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    /**
     * Customer's full name
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Customer's company name
     * For corporate customers (Argon) or individuals
     */
    @Column(name = "company", length = 150)
    private String company;

    /**
     * Business Unit (BU) name
     * Can be: STORE (HP Wolf Security Store), ARGON (Corporate), etc.
     * Used for multi-tenancy and data isolation
     */
    @Column(name = "bu_name", nullable = false, length = 50)
    private String buName;

    /**
     * API Key ID assigned during onboarding
     * Used for authentication via APIGEE
     */
    @Column(name = "api_key_id", nullable = false, unique = true, length = 100)
    private String apiKeyId;

    /**
     * API Secret Key assigned during onboarding
     * Should be stored encrypted in production
     * Used for HMAC signature verification
     */
    @Column(name = "api_secret_key", nullable = false, length = 200)
    private String apiSecretKey;

    /**
     * Customer's phone number (optional)
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Customer's address
     */
    @Column(name = "address", length = 255)
    private String address;

    /**
     * Country/Region
     */
    @Column(name = "country", length = 50)
    private String country;

    /**
     * Customer status
     * Values: ACTIVE, INACTIVE, SUSPENDED, DELETED
     */
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    /**
     * Timestamp when customer record was created
     * Set automatically when a new customer is onboarded
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when customer record was last updated
     * Updated whenever customer information changes
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Timestamp when customer was deleted (soft delete)
     * NULL if customer is active
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Pre-persist method - Called automatically before saving to database
     * Sets the createdAt and updatedAt timestamps
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Pre-update method - Called automatically before updating in database
     * Updates the updatedAt timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
