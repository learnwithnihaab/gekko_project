package com.hp.gekko.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Subscription Entity - Represents a subscription created for a customer
 * 
 * Subscription is the core concept in GEKKO.
 * Each subscription corresponds to:
 * 1. One software product (HP Wolf Security, Office 365, etc.)
 * 2. One customer
 * 3. One contract in BRIM
 * 4. One or more licenses from QLS
 */
@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "product_sku", nullable = false, length = 50)
    private String productSku;

    @Column(name = "product_name", length = 100)
    private String productName;

    /**
     * Contract Account Number assigned by BRIM
     * Used to link subscription to SAP contract
     */
    @Column(name = "contract_account_number", unique = true, length = 100)
    private String contractAccountNumber;

    /**
     * Sales Order Number from SAP SV or SF
     * Required for license generation
     */
    @Column(name = "sales_order_number", length = 50)
    private String salesOrderNumber;

    /**
     * Subscription Status States:
     * PENDING, ACTIVE, RENEWED, EXPIRED, CANCELLED, SUSPENDED
     */
    @Column(name = "subscription_status", nullable = false, length = 50)
    private String subscriptionStatus = "PENDING";

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate = LocalDate.now();

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;

    /**
     * Auto-renewal flag
     * TRUE: Automatically renew subscription when it expires
     * FALSE: Manual renewal required
     */
    @Column(name = "auto_renewal", nullable = false)
    private Boolean autoRenewal = true;

    /**
     * License key assigned by QLS
     * Used to activate the software
     */
    @Column(name = "license_key", unique = true, length = 200)
    private String licenseKey;

    @Column(name = "license_generated_at")
    private LocalDateTime licenseGeneratedAt;

    @Column(name = "license_activated_at")
    private LocalDateTime licenseActivatedAt;

    @Column(name = "license_quantity")
    private Integer licenseQuantity = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "brim_callback_received_at")
    private LocalDateTime brimCallbackReceivedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.startDate == null) {
            this.startDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
