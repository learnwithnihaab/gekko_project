package com.gekko.service;

import com.gekko.entity.Client;

import java.util.List;

public interface ClientService {
    Client createClient(String name, String apiKey, String apiSecret);
    List<Client> listClients();
    Client findByApiKey(String apiKey);
}
