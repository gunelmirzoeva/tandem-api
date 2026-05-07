package com.example.tandem_api.exception;

public class TimezoneNotSetException extends RuntimeException {
    public TimezoneNotSetException(String message) {
        super(message);
    }
}
