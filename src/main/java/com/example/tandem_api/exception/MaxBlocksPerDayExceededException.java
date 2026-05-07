package com.example.tandem_api.exception;

public class MaxBlocksPerDayExceededException extends RuntimeException {
    public MaxBlocksPerDayExceededException(String message) {
        super(message);
    }
}
