package com.gekko.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * OrderProducer - publishes order created events. For now it publishes string payloads to
 * 'gekko.orders.created'. In production use structured formats (Avro/JSON Schema) and include correlation ids.
 */
@Component
public class OrderProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(String externalId, String payload) {
        kafkaTemplate.send("gekko.orders.created", externalId, payload);
    }
}
