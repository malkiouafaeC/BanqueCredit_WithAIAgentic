package com.minibanque.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.minibanque.backend.dto.ClientResponse;
import com.minibanque.backend.dto.CreateClientRequest;
import com.minibanque.backend.service.ClientService;

@RestController
@RequestMapping("/api/clients")
public class ClientsController {

    private final ClientService clientService;

    public ClientsController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> listClients() {
        return ResponseEntity.ok(clientService.listClients());
    }

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse createClient(@RequestBody CreateClientRequest payload) {
        return clientService.createClient(payload);
    }
}
