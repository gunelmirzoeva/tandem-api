package com.example.tandem_api.exception;

public class LanguageAlreadyInTargetListException extends RuntimeException {
    public LanguageAlreadyInTargetListException(String message) {
        super(message);
    }
}
