package com.example.tandem_api.exception;

public class InvalidTimezoneException extends RuntimeException {
    public InvalidTimezoneException(String message) {
        super(message);
    }
}
