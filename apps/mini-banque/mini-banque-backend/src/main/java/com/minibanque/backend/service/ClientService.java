package com.minibanque.backend.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.minibanque.backend.dto.ClientResponse;
import com.minibanque.backend.dto.CreateClientRequest;
import com.minibanque.backend.exception.ClientValidationException;

@Service
public class ClientService {

    private final CopyOnWriteArrayList<ClientResponse> clients = new CopyOnWriteArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(2);

    public ClientService() {
        clients.add(new ClientResponse(1, "Client A", "a@bank.test"));
        clients.add(new ClientResponse(2, "Client B", "b@bank.test"));
    }

    public List<ClientResponse> listClients() {
        return List.copyOf(clients);
    }

    public ClientResponse createClient(CreateClientRequest request) {
        if (!isValid(request)) {
            throw new ClientValidationException();
        }

        long id = idSequence.incrementAndGet();
        ClientResponse created = new ClientResponse(id, request.nom().trim(), request.email().trim());
        clients.add(created);
        return created;
    }

    private boolean isValid(CreateClientRequest request) {
        if (request == null || request.nom() == null || request.email() == null) {
            return false;
        }

        String nom = request.nom().trim();
        String email = request.email().trim();
        if (nom.isEmpty() || email.isEmpty()) {
            return false;
        }

        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1;
    }
}
