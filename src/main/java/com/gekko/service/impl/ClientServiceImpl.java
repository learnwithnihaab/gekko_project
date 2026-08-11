package com.gekko.service.impl;

import com.gekko.entity.Client;
import com.gekko.repository.ClientRepository;
import com.gekko.service.ClientService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Simple ClientService implementation. In production, store secrets hashed and/or in a
 * secure secrets manager and do not return secrets via APIs. This service is for managing
 * client onboarding inside Gekko (if you don't manage clients externally in APIGEE).
 */
@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Client createClient(String name, String apiKey, String apiSecret) {
        Client c = new Client();
        c.setName(name);
        c.setApiKey(apiKey);
        c.setApiSecret(apiSecret); // TODO: hash in real deployments
        return clientRepository.save(c);
    }

    @Override
    public List<Client> listClients() {
        return clientRepository.findAll();
    }

    @Override
    public Client findByApiKey(String apiKey) {
        return clientRepository.findByApiKey(apiKey).orElse(null);
    }
}
