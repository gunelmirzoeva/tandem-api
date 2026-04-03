package com.example.tandem_api.exception;

public class ResendLimitReachedException extends RuntimeException {
    public ResendLimitReachedException(String message) {
        super(message);
    }
}
