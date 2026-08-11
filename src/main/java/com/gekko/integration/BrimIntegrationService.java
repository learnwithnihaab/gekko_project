package com.gekko.integration;

import com.gekko.entity.BrimOutboundAttempt;
import com.gekko.entity.OrderEntity;
import com.gekko.service.BrimAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * BrimIntegrationService - updated to persist outbound attempts for admin inspection and retry.
 */
@Service
public class BrimIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(BrimIntegrationService.class);

    private final BrimClient brimClient;
    private final BrimAttemptService attemptService;

    public BrimIntegrationService(BrimClient brimClient, BrimAttemptService attemptService) {
        this.brimClient = brimClient;
        this.attemptService = attemptService;
    }

    public void triggerCreateContract(OrderEntity order) {
        String idempotencyKey = order.getExternalId() != null ? "order-" + order.getExternalId() : java.util.UUID.randomUUID().toString();
        String payload = String.format("{\"externalId\":\"%s\",\"orderId\":%d,\"product\":\"%s\",\"amount\":%s}",
                order.getExternalId(), order.getId(), order.getProductCode(), order.getAmount() == null ? "0" : order.getAmount().toString());

        BrimOutboundAttempt attempt = attemptService.createAttempt(order, idempotencyKey, payload);

        brimClient.createContract(order)
                .doOnSuccess(resp -> {
                    log.info("BRIM create contract succeeded for order {}: {}", order.getExternalId(), resp);
                    attemptService.markSuccess(attempt.getId());
                })
                .doOnError(err -> {
                    log.error("BRIM create contract failed for order {}", order.getExternalId(), err);
                    attemptService.markFailure(attempt.getId(), err);
                })
                .subscribe();
    }

    // Admin-triggered retry for a specific attempt
    public void retryAttempt(Long attemptId) {
        BrimOutboundAttempt a = attemptService.findById(attemptId);
        if (a == null) return;
        // Minimal: rebuild an OrderEntity stub to call BrimClient; in production fetch full OrderEntity
        OrderEntity order = new OrderEntity();
        order.setId(a.getOrderId());
        order.setExternalId(a.getOrderExternalId());
        // payload isn't fully parsed; BrimClient will re-create payload from OrderEntity; adjust as needed

        // createAttempt entry updated via createAttempt in triggerCreateContract; here we call createContract and update attempt
        BrimOutboundAttempt retryAttempt = attemptService.createAttempt(order, a.getIdempotencyKey(), a.getPayload());
        brimClient.createContract(order)
                .doOnSuccess(resp -> attemptService.markSuccess(retryAttempt.getId()))
                .doOnError(err -> attemptService.markFailure(retryAttempt.getId(), err))
                .subscribe();
    }
}
