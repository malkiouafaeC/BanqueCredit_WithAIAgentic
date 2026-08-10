package com.minibanque.backend.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.minibanque.backend.dto.LoginResponse;
import com.minibanque.backend.exception.InvalidCredentialsException;

@Service
public class AuthService {

    private static final Map<String, String> HARD_CODED_USERS = Map.of(
            "conseiller1", "password123"
    );

    private final JwtService jwtService;

    public AuthService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public LoginResponse authenticate(String username, String password) {
        String expectedPassword = HARD_CODED_USERS.get(username);
        if (expectedPassword == null || !expectedPassword.equals(password)) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(username);
        return new LoginResponse(token, username);
    }
}
