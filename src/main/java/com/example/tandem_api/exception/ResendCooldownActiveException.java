package com.example.tandem_api.exception;

public class ResendCooldownActiveException extends RuntimeException {
    public ResendCooldownActiveException(String message) {
        super(message);
    }
}
