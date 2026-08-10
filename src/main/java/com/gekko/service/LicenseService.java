package com.gekko.service;

import com.gekko.entity.License;
import com.gekko.entity.Subscription;
import com.gekko.repository.LicenseRepository;
import com.gekko.repository.SubscriptionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * LicenseService handles license lifecycle:
 * - polling for license keys (cron) after BRIM reports contract/account
 * - storing license key once retrieved from PDAPI/QLS
 * This implementation contains a simulated polling mechanism showing where PDAPI integration would be.
 */
@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final SubscriptionRepository subscriptionRepository;

    public LicenseService(LicenseRepository licenseRepository, SubscriptionRepository subscriptionRepository) {
        this.licenseRepository = licenseRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    // Runs every 4 hours as described in requirements to poll for license keys
    @Scheduled(cron = "0 0 */4 * * *")
    public void pollForLicenseKeys() {
        // In real system: query BRIM or PDAPI for subscriptions without license; here we simulate
        List<Subscription> pending = subscriptionRepository.findAll();
        for (Subscription s : pending) {
            // For simplicity, if no license exists create a placeholder request
            Optional<License> existing = licenseRepository.findAll().stream()
                    .filter(l -> l.getSubscription().getId().equals(s.getId()))
                    .findAny();
            if (!existing.isPresent()) {
                License lic = new License();
                lic.setSubscription(s);
                lic.setStatus("PENDING");
                licenseRepository.save(lic);
                // Call PDAPI to request license - omitted. You would call HTTP client here.
            }
        }
    }
}
