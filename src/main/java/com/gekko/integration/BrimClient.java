package com.gekko.integration;

import com.gekko.entity.OrderEntity;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * BrimClient - resilient HTTP client to call BRIM for contract/subscription creation.
 *
 * Behavior:
 * - Sends a POST to {brim.base-url}/contracts with an idempotency header derived from the order externalId.
 * - Uses Resilience4j CircuitBreaker and Retry for basic resilience.
 * - This implementation blocks (WebClient.block()) under the decorated Callable and is intended to be
 *   executed asynchronously from the caller (e.g., via @Async). This keeps the API request fast and
 *   hands the BRIM interaction to a background thread.
 *
 * Notes and production improvements:
 * - Use asynchronous reactive flows instead of blocking when integrating into a reactive pipeline.
 * - Use a per-order idempotency key (externalId) so retries are safe on BRIM side.
 * - Configure CircuitBreaker and Retry parameters via properties for production tuning.
 */
@Component
public class BrimClient {

    private static final Logger log = LoggerFactory.getLogger(BrimClient.class);

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public BrimClient(WebClient.Builder builder,
                      @Value("${brim.base-url:http://brim.local}") String baseUrl,
                      @Value("${brim.circuit.failureRateThreshold:50}") int failureRateThreshold,
                      @Value("${brim.circuit.waitDurationInOpenMs:60000}") long waitDurationInOpenMs) {
        this.webClient = builder.baseUrl(baseUrl).build();

        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofMillis(waitDurationInOpenMs))
                .build();
        this.circuitBreaker = CircuitBreaker.of("brim", cbConfig);

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .build();
        this.retry = Retry.of("brim", retryConfig);
    }

    /**
     * Create contract in BRIM for the given order. Returns the BRIM response body as string.
     * This method uses the order.externalId as idempotency key if present, otherwise generates a UUID.
     */
    public String createContract(OrderEntity order) throws Exception {
        String idempotencyKey = order.getExternalId() != null ? "order-" + order.getExternalId() : UUID.randomUUID().toString();

        String payload = String.format("{\"externalId\":\"%s\",\"orderId\":%d,\"product\":\"%s\",\"amount\":%s}",
                order.getExternalId(), order.getId(), order.getProductCode(), order.getAmount() == null ? "0" : order.getAmount().toString());

        Callable<String> call = () -> {
            log.info("Calling BRIM create contract for order={} idempotency={}", order.getExternalId(), idempotencyKey);
            try {
                // Execute HTTP request and block until result (caller should call this async)
                String resp = webClient.post()
                        .uri(uriBuilder -> uriBuilder.path("/contracts").build())
                        .header("Idempotency-Key", idempotencyKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(Duration.ofSeconds(15));

                log.info("BRIM responded for order {}: {}", order.getExternalId(), resp);
                return resp;
            } catch (Exception ex) {
                log.error("BRIM call failed for order {}", order.getExternalId(), ex);
                throw ex;
            }
        };

        // Decorate the call with retry and circuit breaker
        Callable<String> decorated = CircuitBreaker.decorateCallable(circuitBreaker, Retry.decorateCallable(retry, call));

        try {
            return decorated.call();
        } catch (Exception ex) {
            // Bubble up so caller can handle (and metrics / outbox retry will take care of eventual consistency)
            throw ex;
        }
    }
}
