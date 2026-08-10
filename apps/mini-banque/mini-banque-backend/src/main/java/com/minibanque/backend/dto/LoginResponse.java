package com.minibanque.backend.dto;

public record LoginResponse(
        String token,
        String username
) {
}
