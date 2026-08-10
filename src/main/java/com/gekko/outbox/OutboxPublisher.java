package com.gekko.outbox;

import com.gekko.messaging.OrderProducer;
import com.gekko.outbox.OutboxEvent;
import com.gekko.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OutboxPublisher polls the outbox_events table periodically and publishes events to Kafka.
 *
 * Behavior:
 * - Polls for unpublished events (published = false).
 * - Publishes each event payload to the appropriate topic (currently hard-coded for OrderCreated).
 * - Marks events as published when publish succeeds.
 *
 * Note: This is a simple implementation intended for a starter/demo environment. In production
 * consider stronger guarantees:
 * - Use a single DB transaction to write event and then publish via transaction log or a transactional
 *   outbox -> broker bridge.
 * - Ensure idempotence on consumers; include correlation/trace ids.
 * - Add batching, error handling, exponential backoff and alerting on repeated failures.
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outboxRepository;
    private final OrderProducer orderProducer;

    public OutboxPublisher(OutboxRepository outboxRepository, OrderProducer orderProducer) {
        this.outboxRepository = outboxRepository;
        this.orderProducer = orderProducer;
    }

    /**
     * Poll the outbox every 5 seconds (configurable by the fixedDelayString).
     * Adjust frequency depending on throughput and latency requirements.
     */
    @Scheduled(fixedDelayString = "${outbox.poll.delay:5000}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxRepository.findByPublishedFalse();
        if (pending.isEmpty()) {
            return;
        }

        log.info("Found {} outbox events to publish", pending.size());

        for (OutboxEvent event : pending) {
            try {
                // For this starter we only handle OrderCreated events and publish to 'gekko.orders.created'.
                if ("OrderCreated".equals(event.getType())) {
                    orderProducer.publishOrderCreated(event.getAggregateId(), event.getPayload());
                } else {
                    // If other event types are added later, extend this switch/case.
                    log.warn("Unknown outbox event type: {} - skipping", event.getType());
                }

                // mark published; in a robust system consider optimistic locking and retries
                event.setPublished(Boolean.TRUE);
                outboxRepository.save(event);
                log.info("Published outbox event id={} type={} aggregate={}/{}", event.getId(), event.getType(), event.getAggregateType(), event.getAggregateId());
            } catch (Exception ex) {
                // Don't mark the event published if publishing fails.
                // Log and continue with next event; add alerting for repeated failures.
                log.error("Failed to publish outbox event id={}; will retry later", event.getId(), ex);
            }
        }
    }
}
