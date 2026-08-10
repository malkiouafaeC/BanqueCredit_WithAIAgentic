package com.minibanque.backend.dto;

public record CreateClientRequest(
        String nom,
        String email
) {
}
