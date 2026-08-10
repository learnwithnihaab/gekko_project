package com.gekko.outbox;

import com.gekko.messaging.OrderProducer;
import com.gekko.outbox.OutboxEvent;
import com.gekko.repository.OutboxRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * OutboxPublisher polls the outbox_events table periodically and publishes events to Kafka.
 *
 * Improvements over the previous version:
 * - Batch reads using pagination
 * - Retry loop per event with configurable retry count and simple backoff
 * - Marks events published using an atomic UPDATE (markPublishedById) to avoid races
 * - Emits simple Micrometer metrics (published / failed)
 *
 * Production considerations (future): batching to improve throughput, transactional
 * publishing bridges, stronger idempotence and observability.
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outboxRepository;
    private final OrderProducer orderProducer;
    private final MeterRegistry meterRegistry;

    private final Counter publishedCounter;
    private final Counter failedCounter;

    public OutboxPublisher(OutboxRepository outboxRepository, OrderProducer orderProducer, MeterRegistry meterRegistry) {
        this.outboxRepository = outboxRepository;
        this.orderProducer = orderProducer;
        this.meterRegistry = meterRegistry;

        this.publishedCounter = meterRegistry.counter("outbox.published.count");
        this.failedCounter = meterRegistry.counter("outbox.failed.count");
    }

    /**
     * Poll the outbox using pagination and process in small batches.
     * The poll delay and batch size are configurable via application.yml (outbox.poll.delay, outbox.batch.size).
     */
    @Scheduled(fixedDelayString = "${outbox.poll.delay:5000}")
    public void publishPending() {
        int batchSize = Integer.parseInt(System.getProperty("outbox.batch.size", "50"));
        int retryCount = Integer.parseInt(System.getProperty("outbox.retry.count", "3"));

        PageRequest pageRequest = PageRequest.of(0, batchSize);
        Page<OutboxEvent> page = outboxRepository.findByPublishedFalse(pageRequest);

        if (page == null || page.isEmpty()) {
            return;
        }

        List<OutboxEvent> pending = page.getContent();
        log.info("Found {} outbox events to publish (batchSize={})", pending.size(), batchSize);

        for (OutboxEvent event : pending) {
            boolean success = false;

            for (int attempt = 1; attempt <= retryCount; attempt++) {
                try {
                    if ("OrderCreated".equals(event.getType())) {
                        orderProducer.publishOrderCreated(event.getAggregateId(), event.getPayload());
                    } else {
                        log.warn("Unknown outbox event type: {} - skipping", event.getType());
                    }

                    // Mark published; this uses a SQL update to avoid race conditions
                    int updated = outboxRepository.markPublishedById(event.getId());
                    if (updated > 0) {
                        publishedCounter.increment();
                        log.info("Published outbox event id={} type={} aggregate={}/{}", event.getId(), event.getType(), event.getAggregateType(), event.getAggregateId());
                        success = true;
                    } else {
                        // Another process might have published it concurrently
                        log.info("Outbox event id={} was already published by another worker", event.getId());
                        success = true;
                    }

                    break; // success, exit retry loop
                } catch (Exception ex) {
                    log.warn("Attempt {} failed to publish outbox event id={}: {}", attempt, event.getId(), ex.getMessage());
                    if (attempt == retryCount) {
                        failedCounter.increment();
                        log.error("Failed to publish outbox event id={} after {} attempts", event.getId(), retryCount, ex);
                    } else {
                        // simple backoff
                        try { Thread.sleep(Duration.ofSeconds(attempt).toMillis()); } catch (InterruptedException ignored) {}
                    }
                }
            }

            if (!success) {
                // leave it unpublished for later retry; optionally increment a failure counter in DB
            }
        }
    }
}
