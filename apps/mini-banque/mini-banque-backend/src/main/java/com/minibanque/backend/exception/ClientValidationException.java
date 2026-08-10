package com.minibanque.backend.exception;

public class ClientValidationException extends RuntimeException {
    public ClientValidationException() {
        super("Donnees client invalides");
    }
}
