package com.gekko.client;

import com.gekko.entity.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * PDAPI client stub. In a production system this would perform HTTP calls to PDAPI/QLS to request license keys.
 * Here we simulate the call and return a generated license key for demonstration.
 */
@Component
public class PdapiClient {

    private final Logger log = LoggerFactory.getLogger(PdapiClient.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public String requestLicense(Subscription subscription) {
        // TODO: Replace with real PDAPI HTTP request using restTemplate.exchange(...) and proper error handling
        log.info("Requesting license from PDAPI for subscription id {}", subscription.getId());

        // Simulate a synchronous license generation - return a UUID-based key
        String licenseKey = "LIC-" + UUID.randomUUID().toString();
        log.info("Simulated PDAPI returned license key {} for subscription {}", licenseKey, subscription.getId());
        return licenseKey;
    }
}
