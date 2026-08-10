package com.hp.gekko.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Entity - Represents an order placed by a customer
 * 
 * This entity tracks orders from upstream systems (Store, Argon)
 * through the entire GEKKO processing pipeline.
 * 
 * Database Table: orders
 * Status Flow: RECEIVED -> PROCESSING -> SUBSCRIPTION_CREATED -> SENT_TO_BRIM -> COMPLETED/FAILED
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "sales_order_number", unique = true, length = 50)
    private String salesOrderNumber;

    @Column(name = "product_sku", nullable = false, length = 50)
    private String productSku;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    /**
     * Order Status - Tracks the order through the processing pipeline
     * States: RECEIVED, PROCESSING, SUBSCRIPTION_CREATED, SENT_TO_BRIM, BRIM_PROCESSING, 
     *         CONTRACT_CREATED, LICENSE_REQUESTED, LICENSE_GENERATED, COMPLETED, FAILED, CANCELLED
     */
    @Column(name = "order_status", nullable = false, length = 50)
    private String orderStatus = "RECEIVED";

    @Column(name = "gtr_status", length = 50)
    private String gtrStatus = "HELD";

    @Column(name = "brim_status", length = 50)
    private String brimStatus = "PENDING";

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus = "PENDING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "sent_to_brim_at")
    private LocalDateTime sentToBrimAt;

    @Column(name = "brim_response_at")
    private LocalDateTime brimResponseAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
