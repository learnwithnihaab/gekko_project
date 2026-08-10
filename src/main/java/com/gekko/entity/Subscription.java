package com.gekko.entity;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * Subscription entity represents the recurring subscription in Gekko.
 * After BRIM creates a contract and returns contract/account info, we persist subscription details.
 */
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true)
    private String externalId; // optional upstream subscription id

    @OneToOne
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    private String contractAccount; // value returned by BRIM

    private OffsetDateTime startDate;

    private OffsetDateTime endDate;

    private Boolean autorenew = true;

    private String status; // PENDING, ACTIVE, CANCELLED

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // getters/setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    public String getContractAccount() {
        return contractAccount;
    }

    public void setContractAccount(String contractAccount) {
        this.contractAccount = contractAccount;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public Boolean getAutorenew() {
        return autorenew;
    }

    public void setAutorenew(Boolean autorenew) {
        this.autorenew = autorenew;
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
