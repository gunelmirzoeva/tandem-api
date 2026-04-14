package com.example.tandem_api.exception;

public class ReplayAttackDetectedException extends RuntimeException {
    public ReplayAttackDetectedException(String message) {
        super(message);
    }
}
