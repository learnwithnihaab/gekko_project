package com.gekko.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * SubscriptionProducer - publishes subscription created events to 'gekko.subscriptions.created'.
 */
@Component
public class SubscriptionProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public SubscriptionProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishSubscriptionCreated(String subscriptionExternalId, String payload) {
        kafkaTemplate.send("gekko.subscriptions.created", subscriptionExternalId, payload);
    }
}
