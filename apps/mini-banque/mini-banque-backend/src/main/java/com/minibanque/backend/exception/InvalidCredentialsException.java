package com.minibanque.backend.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Username ou password invalide");
    }
}
