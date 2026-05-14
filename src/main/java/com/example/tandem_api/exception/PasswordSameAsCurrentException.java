package com.example.tandem_api.exception;

public class PasswordSameAsCurrentException extends RuntimeException {
    public PasswordSameAsCurrentException(String message) {
        super(message);
    }
}
