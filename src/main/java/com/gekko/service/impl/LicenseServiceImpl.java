package com.gekko.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gekko.entity.License;
import com.gekko.entity.Subscription;
import com.gekko.integration.PdapiClient;
import com.gekko.repository.LicenseRepository;
import com.gekko.repository.SubscriptionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class LicenseServiceImpl implements com.gekko.service.LicenseService {

    private static final Logger log = LoggerFactory.getLogger(LicenseServiceImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final LicenseRepository licenseRepository;
    private final PdapiClient pdapiClient;
    private final ObjectMapper objectMapper;
    private final Counter licenseRequestedCounter;
    private final Counter licenseFailedCounter;

    public LicenseServiceImpl(SubscriptionRepository subscriptionRepository,
                              LicenseRepository licenseRepository,
                              PdapiClient pdapiClient,
                              ObjectMapper objectMapper,
                              MeterRegistry meterRegistry) {
        this.subscriptionRepository = subscriptionRepository;
        this.licenseRepository = licenseRepository;
        this.pdapiClient = pdapiClient;
        this.objectMapper = objectMapper;
        this.licenseRequestedCounter = meterRegistry.counter("pdapi.requested.count");
        this.licenseFailedCounter = meterRegistry.counter("pdapi.failed.count");
    }

    @Override
    public void requestLicenseForSubscription(String subscriptionExternalId) {
        Subscription subscription = subscriptionRepository.findAll().stream()
                .filter(s -> subscriptionExternalId.equals(s.getExternalId()))
                .findFirst().orElse(null);

        if (subscription == null) {
            log.warn("Subscription not found for externalId {}", subscriptionExternalId);
            return;
        }

        requestLicenseInternal(subscription);
    }

    private void requestLicenseInternal(Subscription subscription) {
        String contractAccount = subscription.getContractAccount();
        if (contractAccount == null) {
            log.warn("No contractAccount for subscription {}", subscription.getExternalId());
            return;
        }

        licenseRequestedCounter.increment();

        try {
            Mono<String> mono = pdapiClient.requestLicense(contractAccount);
            String licenseKey = mono.block(); // blocking for simplicity; consider reactive chain

            License license = new License();
            license.setSubscription(subscription);
            license.setLicenseKey(licenseKey);
            license.setStatus("ISSUED");
            license.setIssuedAt(OffsetDateTime.now());
            licenseRepository.save(license);

            log.info("License obtained for subscription {}: {}", subscription.getExternalId(), licenseKey);
        } catch (Exception ex) {
            licenseFailedCounter.increment();
            log.error("Failed to obtain license for subscription {}", subscription.getExternalId(), ex);
        }
    }

    /**
     * Scheduled fallback: poll for subscriptions without a license (status pending) every 4 hours.
     * Cron expression: At minute 0 past every 4th hour.
     */
    @Scheduled(cron = "0 0 */4 * * *")
    public void pollPendingLicenses() {
        log.info("LicenseService: running scheduled poll for pending licenses");

        List<Subscription> subs = subscriptionRepository.findAll();
        for (Subscription s : subs) {
            // Check if subscription already has a license
            boolean hasLicense = licenseRepository.findAll().stream().anyMatch(l -> l.getSubscription().getId().equals(s.getId()));
            if (!hasLicense && "ACTIVE".equals(s.getStatus())) {
                requestLicenseInternal(s);
            }
        }
    }
}
