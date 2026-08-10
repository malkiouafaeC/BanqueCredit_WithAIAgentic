package com.minibanque.backend.dto;

public record ClientResponse(
        long id,
        String nom,
        String email
) {
}
