package com.gekko.integration;

import com.gekko.entity.OrderEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * BrimIntegrationService - wrapper around BrimClient to run BRIM calls asynchronously.
 *
 * The createContract call is executed in a background thread so API latency is not impacted.
 * Results are logged; persistent side effects (subscription creation) will usually arrive via BRIM webhook.
 */
@Service
public class BrimIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(BrimIntegrationService.class);

    private final BrimClient brimClient;

    public BrimIntegrationService(BrimClient brimClient) {
        this.brimClient = brimClient;
    }

    @Async
    public void triggerCreateContract(OrderEntity order) {
        try {
            String resp = brimClient.createContract(order);
            log.info("BRIM create contract call succeeded for order {}: {}", order.getExternalId(), resp);
        } catch (Exception ex) {
            log.error("BRIM create contract call failed for order {}", order.getExternalId(), ex);
            // In production add metrics/alerting and possibly enqueue a retry/outbox event for BRIM-specific integration
        }
    }
}
