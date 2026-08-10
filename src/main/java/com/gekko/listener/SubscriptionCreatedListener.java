package com.gekko.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gekko.entity.License;
import com.gekko.entity.Subscription;
import com.gekko.integration.PdapiClient;
import com.gekko.repository.LicenseRepository;
import com.gekko.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * Listens to subscription created events and requests licenses from PDAPI (QLS) for the subscription.
 * This demonstrates internal microservice communication via Kafka.
 */
@Component
public class SubscriptionCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionCreatedListener.class);

    private final ObjectMapper objectMapper;
    private final PdapiClient pdapiClient;
    private final SubscriptionRepository subscriptionRepository;
    private final LicenseRepository licenseRepository;

    public SubscriptionCreatedListener(ObjectMapper objectMapper, PdapiClient pdapiClient, SubscriptionRepository subscriptionRepository, LicenseRepository licenseRepository) {
        this.objectMapper = objectMapper;
        this.pdapiClient = pdapiClient;
        this.subscriptionRepository = subscriptionRepository;
        this.licenseRepository = licenseRepository;
    }

    @KafkaListener(topics = "gekko.subscriptions.created", groupId = "gekko-service-group")
    public void onSubscriptionCreated(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String subscriptionExternalId = node.path("subscriptionExternalId").asText();
            String contractAccount = node.path("contractAccount").asText();

            log.info("Received subscription.created for subscriptionExternalId={}, contractAccount={}", subscriptionExternalId, contractAccount);

            Subscription subscription = subscriptionRepository.findAll().stream()
                    .filter(s -> subscriptionExternalId.equals(s.getExternalId()))
                    .findFirst()
                    .orElse(null);

            if (subscription == null) {
                log.warn("Subscription not found for externalId {}", subscriptionExternalId);
                return;
            }

            // Request license from PDAPI (blocking for simplicity)
            Mono<String> licenseMono = pdapiClient.requestLicense(contractAccount == null ? subscription.getContractAccount() : contractAccount);
            String licenseKey = licenseMono.block();

            // Persist license
            License license = new License();
            license.setSubscription(subscription);
            license.setLicenseKey(licenseKey);
            license.setStatus("ISSUED");
            license.setIssuedAt(OffsetDateTime.now());
            licenseRepository.save(license);

            log.info("License issued and saved for subscription {}: {}", subscriptionExternalId, licenseKey);
        } catch (Exception ex) {
            log.error("Error processing subscription created message", ex);
        }
    }
}
