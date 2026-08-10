package com.minibanque.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.minibanque.backend.dto.LoginResponse;
import com.minibanque.backend.exception.InvalidCredentialsException;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(new JwtService());
    }

    @Test
    void shouldAuthenticateWithValidCredentials() {
        LoginResponse response = authService.authenticate("conseiller1", "password123");

        assertEquals("conseiller1", response.username());
        assertNotNull(response.token());
    }

    @Test
    void shouldThrowWhenCredentialsAreInvalid() {
        assertThrows(InvalidCredentialsException.class,
                () -> authService.authenticate("conseiller1", "bad"));
    }
}
