package com.gekko.integration;

import com.gekko.entity.OrderEntity;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

/**
 * Reactive BrimClient - non-blocking WebClient integration with BRIM.
 * - Returns a Mono<String> with the BRIM response body.
 * - Applies Reactor retry/backoff and Resilience4j circuit-breaker operator.
 * - Emits Micrometer metrics for success/failure and call time.
 */
@Component
public class BrimClient {

    private static final Logger log = LoggerFactory.getLogger(BrimClient.class);

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    private final int retryAttempts;
    private final int retryWaitSeconds;
    private final int timeoutSeconds;

    private final Counter successCounter;
    private final Counter failureCounter;
    private final Timer timer;

    public BrimClient(WebClient.Builder builder,
                      @Value("${brim.base-url}") String baseUrl,
                      @Value("${brim.circuit.failure-rate-threshold}") int failureRateThreshold,
                      @Value("${brim.circuit.wait-duration-in-open-ms}") long waitDurationInOpenMs,
                      @Value("${brim.retry.attempts}") int retryAttempts,
                      @Value("${brim.retry.wait-seconds}") int retryWaitSeconds,
                      @Value("${brim.timeout-seconds}") int timeoutSeconds,
                      MeterRegistry meterRegistry) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.retryAttempts = retryAttempts;
        this.retryWaitSeconds = retryWaitSeconds;
        this.timeoutSeconds = timeoutSeconds;

        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofMillis(waitDurationInOpenMs))
                .build();
        this.circuitBreaker = CircuitBreaker.of("brim", cbConfig);

        this.successCounter = meterRegistry.counter("brim.calls.success");
        this.failureCounter = meterRegistry.counter("brim.calls.failure");
        this.timer = meterRegistry.timer("brim.calls.latency");
    }

    public Mono<String> createContract(OrderEntity order) {
        String idempotencyKey = order.getExternalId() != null ? "order-" + order.getExternalId() : UUID.randomUUID().toString();

        String payload = String.format("{\"externalId\":\"%s\",\"orderId\":%d,\"product\":\"%s\",\"amount\":%s}",
                order.getExternalId(), order.getId(), order.getProductCode(), order.getAmount() == null ? "0" : order.getAmount().toString());

        Mono<String> call = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/contracts").build())
                .header("Idempotency-Key", idempotencyKey)
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(retryWaitSeconds)).maxBackoff(Duration.ofSeconds(retryWaitSeconds * 4)));

        return timer.recordWhen(call.doOnSuccess(r -> successCounter.increment()).doOnError(e -> failureCounter.increment()));
    }
}
