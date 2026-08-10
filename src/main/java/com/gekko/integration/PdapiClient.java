package com.gekko.integration;

import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * PdapiClient - resilient WebClient-based client to request licenses from PDAPI/QLS.
 * It applies timeouts and retries and returns a Mono<String> containing the license key.
 * Replace baseUrl and endpoint paths with real PDAPI details when available.
 */
@Component
public class PdapiClient {

    private static final Logger log = LoggerFactory.getLogger(PdapiClient.class);

    private final WebClient webClient;
    private final String basePath;

    public PdapiClient(WebClient.Builder builder, @Value("${pdapi.base-url:http://pdapi.local}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.basePath = baseUrl;
    }

    /**
     * Request a license for the given contract account. The method tries a few retries with backoff and
     * times out if PDAPI does not respond.
     */
    public Mono<String> requestLicense(String contractAccount) {
        if (contractAccount == null) {
            return Mono.error(new IllegalArgumentException("contractAccount is required"));
        }

        // Example request payload - adjust to PDAPI contract
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/licenses").build())
                .bodyValue("{\"contractAccount\":\"" + contractAccount + "\"}")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).maxBackoff(Duration.ofSeconds(8)))
                .doOnError(err -> log.error("PDAPI request failed for {}: {}", contractAccount, err.toString()));
    }
}
