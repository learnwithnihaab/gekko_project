package com.gekko.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * PdapiClient - placeholder client to call PDAPI/QLS for license generation.
 * Replace baseUrl and implement real contract for PDAPI.
 */
@Component
public class PdapiClient {
    private final WebClient webClient;

    public PdapiClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://pdapi.local").build();
    }

    public Mono<String> requestLicense(String contractAccount) {
        return Mono.just("LICENSE-KEY-123456");
    }
}
