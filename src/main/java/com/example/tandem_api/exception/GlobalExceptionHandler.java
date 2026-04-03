package com.example.tandem_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.tandem_api.dto.ErrorResponse;

import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid value"))
                .findFirst()
                .orElse("Validation failed");
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(EmailAlreadyExistsException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ErrorResponse> handleOtpExpiredException(OtpExpiredException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.GONE);
    }

    @ExceptionHandler(OtpInvalidException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(OtpInvalidException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OtpInvalidatedAfterAttemptsException.class)
    public ResponseEntity<ErrorResponse> handleLocked(OtpInvalidatedAfterAttemptsException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.LOCKED);
    }

    @ExceptionHandler({
            ResendCooldownActiveException.class,
            ResendLimitReachedException.class
    })
    public ResponseEntity<ErrorResponse> handleLimitExceed(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return new ResponseEntity<>(new ErrorResponse("An unexpected error occurred."), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
