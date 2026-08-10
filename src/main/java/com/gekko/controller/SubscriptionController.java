package com.gekko.controller;

import com.gekko.entity.Subscription;
import com.gekko.repository.SubscriptionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Subscription APIs exposed to upstream systems to query subscription data.
 * These endpoints are read-heavy so we demonstrate @Cacheable for quick responses.
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionController(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @GetMapping
    @Cacheable("subscriptions_all")
    public ResponseEntity<List<Subscription>> getAll() {
        return ResponseEntity.ok(subscriptionRepository.findAll());
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<Subscription> getByExternalId(@PathVariable String externalId) {
        return subscriptionRepository.findByExternalId(externalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
