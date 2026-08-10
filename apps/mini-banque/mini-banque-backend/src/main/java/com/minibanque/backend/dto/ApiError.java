package com.minibanque.backend.dto;

public record ApiError(
        String code,
        String message
) {
}
