package com.minibanque.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.minibanque.backend.dto.CreateClientRequest;
import com.minibanque.backend.exception.ClientValidationException;

class ClientServiceTest {

    private ClientService clientService;

    @BeforeEach
    void setUp() {
        clientService = new ClientService();
    }

    @Test
    void shouldReturnHardcodedClients() {
        var clients = clientService.listClients();

        assertEquals(2, clients.size());
        assertEquals("Client A", clients.get(0).nom());
        assertEquals("Client B", clients.get(1).nom());
    }

    @Test
    void shouldCreateClientWithValidPayload() {
        var created = clientService.createClient(new CreateClientRequest("Client C", "c@bank.test"));

        assertEquals(3L, created.id());
        assertEquals("Client C", created.nom());
        assertEquals("c@bank.test", created.email());
    }

    @Test
    void shouldThrowForInvalidPayload() {
        assertThrows(ClientValidationException.class,
                () -> clientService.createClient(new CreateClientRequest("", "invalid")));
    }
}
