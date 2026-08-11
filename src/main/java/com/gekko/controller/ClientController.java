package com.gekko.controller;

import com.gekko.entity.Client;
import com.gekko.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * Client onboarding controller - allows creating API client credentials for upstream systems.
 * This endpoint should be restricted to admin users only in production.
 */
@RestController
@RequestMapping("/internal/admin/clients")
public class ClientController {

    private final ClientService clientService;
    private final SecureRandom secureRandom = new SecureRandom();

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createClient(@RequestParam String name) {
        // Generate apiKey and secret. For simplicity we generate random base64 strings.
        String apiKey = "client-" + Math.abs(secureRandom.nextInt());
        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        String apiSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);

        Client c = clientService.createClient(name, apiKey, apiSecret);

        // In production the secret should be shown only once, and hashed in DB.
        return ResponseEntity.ok(List.of(
                "apiKey: " + c.getApiKey(),
                "apiSecret: " + c.getApiSecret()
        ));
    }

    @GetMapping
    public ResponseEntity<?> listClients() {
        List<Client> clients = clientService.listClients();
        return ResponseEntity.ok(clients);
    }
}
