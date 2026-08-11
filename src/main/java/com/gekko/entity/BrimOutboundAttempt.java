package com.gekko.entity;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * BrimOutboundAttempt - records outbound attempts to BRIM so admins can inspect and retry.
 */
@Entity
@Table(name = "brim_outbound_attempts")
public class BrimOutboundAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private String orderExternalId;

    private String idempotencyKey;

    @Column(columnDefinition = "text")
    private String payload;

    private String status; // PENDING, SUCCESS, FAILED

    private Integer attempts = 0;

    private OffsetDateTime lastAttemptAt;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderExternalId() { return orderExternalId; }
    public void setOrderExternalId(String orderExternalId) { this.orderExternalId = orderExternalId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public OffsetDateTime getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(OffsetDateTime lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
