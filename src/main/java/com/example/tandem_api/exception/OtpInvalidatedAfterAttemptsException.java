package com.example.tandem_api.exception;

public class OtpInvalidatedAfterAttemptsException extends RuntimeException {
    public OtpInvalidatedAfterAttemptsException(String message) {
        super(message);
    }
}
