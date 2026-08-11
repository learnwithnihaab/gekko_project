package com.gekko.entity;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * Client entity represents an onboarded upstream client (Storefront, Argon, etc.).
 * Onboarding creates an apiKey and secret which the client may use to authenticate
 * requests directly (if APIGEE doesn't emit JWT). In production you may prefer
 * to manage clients in APIGEE or a dedicated identity provider.
 */
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A friendly name for the client (e.g., "HP Storefront" or "Argon")
    @Column(nullable = false)
    private String name;

    // Public API key used as username
    @Column(name = "api_key", unique = true, nullable = false)
    private String apiKey;

    // Secret is stored as hashed value in production; for the starter we store plain text but
    // the code comments explain how to replace with a proper hash/secret manager.
    @Column(name = "api_secret", nullable = false)
    private String apiSecret;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
