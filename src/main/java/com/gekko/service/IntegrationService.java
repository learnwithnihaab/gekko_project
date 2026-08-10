package com.gekko.service;

import com.gekko.client.PdapiClient;
import com.gekko.entity.License;
import com.gekko.entity.OrderEntity;
import com.gekko.entity.Subscription;
import com.gekko.repository.LicenseRepository;
import com.gekko.repository.OrderRepository;
import com.gekko.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * IntegrationService centralizes interactions with downstream systems: BRIM and PDAPI (QLS).
 * It contains logic to process contract-created events from BRIM and to request licenses from PDAPI.
 */
@Service
public class IntegrationService {

    private final Logger log = LoggerFactory.getLogger(IntegrationService.class);
    private final SubscriptionRepository subscriptionRepository;
    private final OrderRepository orderRepository;
    private final LicenseRepository licenseRepository;
    private final PdapiClient pdapiClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String LICENSE_REQUEST_TOPIC = "license-requests";

    public IntegrationService(SubscriptionRepository subscriptionRepository,
                              OrderRepository orderRepository,
                              LicenseRepository licenseRepository,
                              PdapiClient pdapiClient,
                              KafkaTemplate<String, Object> kafkaTemplate) {
        this.subscriptionRepository = subscriptionRepository;
        this.orderRepository = orderRepository;
        this.licenseRepository = licenseRepository;
        this.pdapiClient = pdapiClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void handleBrimContractEvent(Map<String, Object> event) {
        // 1. Extract order external id from event
        String orderExternalId = (String) event.get("orderExternalId");
        if (orderExternalId == null) {
            log.warn("BRIM event missing orderExternalId: {}", event);
            return;
        }

        // 2. Find order & subscription
        Optional<OrderEntity> optOrder = orderRepository.findByExternalId(orderExternalId);
        if (!optOrder.isPresent()) {
            log.warn("Order not found for externalId {}", orderExternalId);
            return;
        }
        OrderEntity order = optOrder.get();

        // In our model subscription is linked to order via one-to-one
        Subscription subscription = subscriptionRepository.findAll().stream()
                .filter(s -> s.getOrder() != null && s.getOrder().getId().equals(order.getId()))
                .findFirst().orElse(null);

        if (subscription == null) {
            log.warn("Subscription not found for order {}", orderExternalId);
            return;
        }

        // 3. Update subscription with contract values returned by BRIM
        String contractAccount = (String) event.get("contractAccount");
        if (contractAccount != null) {
            subscription.setContractAccount(contractAccount);
        }

        // parse dates if present (simple parse omitted for brevity) - set approximate values
        subscription.setStartDate(OffsetDateTime.now());
        subscription.setEndDate(OffsetDateTime.now().plusYears(1));

        subscription.setStatus("ACTIVE");
        subscriptionRepository.save(subscription);

        // 4. Once BRIM confirms contract, request license from PDAPI
        // We can either publish a license request event to a topic or call PDAPI directly.
        // Here we call PDAPI client synchronously for demo purposes and persist the returned license.
        try {
            String licenseKey = pdapiClient.requestLicense(subscription);
            if (licenseKey != null) {
                License lic = new License();
                lic.setSubscription(subscription);
                lic.setLicenseKey(licenseKey);
                lic.setStatus("GENERATED");
                licenseRepository.save(lic);

                // Optionally publish license-event for other systems
                kafkaTemplate.send("license-events", subscription.getOrder().getExternalId(), lic);
            } else {
                // If PDAPI returns no license immediately, create a PENDING license so poller can pick it up
                License lic = new License();
                lic.setSubscription(subscription);
                lic.setStatus("PENDING");
                licenseRepository.save(lic);

                // Publish a license request event so asynchronous processors can pick it up
                kafkaTemplate.send(LICENSE_REQUEST_TOPIC, subscription.getOrder().getExternalId(), subscription.getOrder().getExternalId());
            }
        } catch (Exception e) {
            log.error("Failed to request license for subscription {}", subscription.getId(), e);
            // Persist a FAILED license record to support admin retries
            License lic = new License();
            lic.setSubscription(subscription);
            lic.setStatus("FAILED");
            licenseRepository.save(lic);
        }
    }
}
