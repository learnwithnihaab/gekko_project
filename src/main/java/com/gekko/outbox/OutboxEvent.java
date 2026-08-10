package com.gekko.outbox;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * OutboxEvent - simple outbox table to store events that need to be published to Kafka.
 * This is part of an eventual outbox pattern ensuring DB + message durability.
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType; // e.g., Order
    private String aggregateId; // entity id or external id
    private String type; // event type, e.g., OrderCreated

    @Column(columnDefinition = "jsonb")
    private String payload; // JSON payload

    @Column(name = "published")
    private Boolean published = Boolean.FALSE;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
