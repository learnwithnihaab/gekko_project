package com.gekko.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * BrimClient - placeholder client that would call BRIM APIs.
 * In production this should implement retries, timeouts, circuit breakers and proper authentication.
 */
@Component
public class BrimClient {

    private final WebClient webClient;

    public BrimClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://brim.local").build();
    }

    public Mono<String> createContract(String orderPayload) {
        // For now we simulate by returning a Mono.just
        return Mono.just("BRIM-ACK");
    }
}
