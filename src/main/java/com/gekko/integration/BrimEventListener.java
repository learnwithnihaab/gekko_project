package com.gekko.integration;

import com.gekko.service.IntegrationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Listens to events produced by BRIM. In production, BRIM would publish to a topic
 * (brim-contract-events) after it completes contract creation. Gekko consumes that
 * event and updates subscription state accordingly.
 */
@Component
public class BrimEventListener {

    private final IntegrationService integrationService;

    public BrimEventListener(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @KafkaListener(topics = "brim-contract-events", groupId = "gekko-group")
    public void onBrimEvent(Map<String, Object> event) {
        // Example event payload expected fields:
        // { "orderExternalId": "SO12345", "contractAccount": "CA-9876", "startDate": "2026-08-10T00:00:00Z", "endDate": "2027-08-09T23:59:59Z" }
        // We forward it to the IntegrationService which handles business logic and persistence updates.
        integrationService.handleBrimContractEvent(event);
    }
}
