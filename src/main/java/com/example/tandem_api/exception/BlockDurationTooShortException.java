package com.example.tandem_api.exception;

public class BlockDurationTooShortException extends RuntimeException {
    public BlockDurationTooShortException(String message) {
        super(message);
    }
}
