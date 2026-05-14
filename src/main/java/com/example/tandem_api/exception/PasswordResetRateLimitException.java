package com.example.tandem_api.exception;

public class PasswordResetRateLimitException extends RuntimeException {
    public PasswordResetRateLimitException(String message) {
        super(message);
    }
}
