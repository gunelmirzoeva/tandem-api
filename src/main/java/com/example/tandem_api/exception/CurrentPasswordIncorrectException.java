package com.example.tandem_api.exception;

public class CurrentPasswordIncorrectException extends RuntimeException {
    public CurrentPasswordIncorrectException(String message) {
        super(message);
    }
}
