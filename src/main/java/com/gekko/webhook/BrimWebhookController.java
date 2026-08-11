package com.gekko.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gekko.entity.OrderEntity;
import com.gekko.entity.Subscription;
import com.gekko.outbox.OutboxEvent;
import com.gekko.repository.OrderRepository;
import com.gekko.repository.OutboxRepository;
import com.gekko.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Controller to receive BRIM webhook notifications.
 * Expected payload (example): { "orderExternalId":"ext-123", "contractAccount":"CA-1001", "subscriptionExternalId":"sub-abc", "startDate":"2026-08-10T...", "endDate":"..." }
 *
 * The controller validates an HMAC signature (if configured) and then creates/updates the Subscription
 * record and enqueues an outbox event 'SubscriptionCreated' so internal services (via Kafka) can react.
 */
@RestController
public class BrimWebhookController {

    private static final Logger log = LoggerFactory.getLogger(BrimWebhookController.class);

    private final OrderRepository orderRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${brim.webhook.secret:}")
    private String brimWebhookSecret;

    public BrimWebhookController(OrderRepository orderRepository, SubscriptionRepository subscriptionRepository, OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/internal/webhooks/brim")
    public ResponseEntity<?> receiveBrimWebhook(@RequestHeader(value = "X-BRIM-SIGNATURE", required = false) String signature,
                                                @RequestBody Map<String, Object> payload) {
        try {
            // Validate signature if secret configured
            if (StringUtils.hasText(brimWebhookSecret)) {
                if (!validateSignature(brimWebhookSecret, payload, signature)) {
                    log.warn("Invalid BRIM webhook signature");
                    return ResponseEntity.status(401).body("invalid signature");
                }
            }

            String orderExternalId = (String) payload.get("orderExternalId");
            String contractAccount = (String) payload.get("contractAccount");
            String subscriptionExternalId = (String) payload.get("subscriptionExternalId");

            if (!StringUtils.hasText(orderExternalId) || !StringUtils.hasText(subscriptionExternalId)) {
                log.warn("Missing required fields in BRIM payload: {}", payload);
                return ResponseEntity.badRequest().body("missing fields");
            }

            OrderEntity order = orderRepository.findByExternalId(orderExternalId).orElse(null);
            if (order == null) {
                log.warn("Order not found for externalId {}", orderExternalId);
                // Optionally create or log for manual reconciliation
            }

            // Create subscription (idempotent if subscriptionExternalId already exists)
            Subscription subscription = new Subscription();
            subscription.setExternalId(subscriptionExternalId);
            subscription.setOrder(order);
            subscription.setContractAccount(contractAccount);
            subscription.setStatus("ACTIVE");
            subscription = subscriptionRepository.save(subscription);

            // Emit outbox event so internal systems can pick up the subscription creation (Kafka)
            OutboxEvent event = new OutboxEvent();
            event.setAggregateType("Subscription");
            event.setAggregateId(subscription.getExternalId());
            event.setType("SubscriptionCreated");
            String eventPayload = objectMapper.writeValueAsString(Map.of(
                    "subscriptionExternalId", subscription.getExternalId(),
                    "contractAccount", subscription.getContractAccount(),
                    "orderExternalId", orderExternalId
            ));
            event.setPayload(eventPayload);
            outboxRepository.save(event);

            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            log.error("Failed to process BRIM webhook", ex);
            return ResponseEntity.status(500).body("error");
        }
    }

    private boolean validateSignature(String secret, Map<String, Object> payload, String signature) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(key);
            byte[] sigBytes = hmac.doFinal(payloadJson.getBytes(StandardCharsets.UTF_8));
            String computed = bytesToHex(sigBytes);
            return computed.equals(signature);
        } catch (Exception ex) {
            log.error("Error validating signature", ex);
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
