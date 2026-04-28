package com.example.tandem_api.exception;

public class LanguageAlreadyInSpokenListException extends RuntimeException {
    public LanguageAlreadyInSpokenListException(String message) {
        super(message);
    }
}
