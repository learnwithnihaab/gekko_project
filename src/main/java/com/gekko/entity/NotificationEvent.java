package com.gekko.entity;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * NotificationEvent stores external notifications we receive (BRIM callbacks, payment webhooks).
 * This helps with auditing and retrying failed processing.
 */
@Entity
@Table(name = "notification_events")
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source; // e.g., BRIM, PDAPI, PAYMENT_GATEWAY

    @Column(columnDefinition = "text")
    private String payload;

    private String status; // RECEIVED, PROCESSED, FAILED

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // getters/setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
