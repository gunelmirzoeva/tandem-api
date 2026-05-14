package com.example.tandem_api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.tandem_api.dto.auth.ErrorResponse;

import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler({
            LanguageAlreadyInSpokenListException.class,
            LanguageAlreadyInTargetListException.class,
            LanguageExistsInOppositeListException.class,
            CannotRemoveLastSpokenLanguageException.class,
            PasswordSameAsCurrentException.class,
            EmailAlreadyExistsException.class,
            UserAlreadyDeactivatedException.class,
            AvailabilityBlockOverlapException.class,
            MaxBlocksPerDayExceededException.class,
            CannotRemoveLastBlockException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ErrorResponse> handleOtpExpiredException(OtpExpiredException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.GONE);
    }

    @ExceptionHandler({
            OtpInvalidException.class,
            InvalidTimezoneException.class,
            TimezoneNotSetException.class,
            InvalidTimeRangeException.class,
            BlockDurationTooShortException.class,
            OtpAlreadyUsedException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OtpInvalidatedAfterAttemptsException.class)
    public ResponseEntity<ErrorResponse> handleLocked(OtpInvalidatedAfterAttemptsException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.LOCKED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            ResendCooldownActiveException.class,
            ResendLimitReachedException.class,
            PasswordResetRateLimitException.class
    })
    public ResponseEntity<ErrorResponse> handleLimitExceed(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler({
            AccountNotVerifiedException.class,
            AccountDeactivatedException.class
    })
    public ResponseEntity<ErrorResponse> handleForbiddenAccountExceptions(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            RefreshTokenExpiredException.class,
            RefreshTokenNotFoundException.class,
            ReplayAttackDetectedException.class,
            InvalidCredentialsException.class,
            CurrentPasswordIncorrectException.class,
    })
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UnsupportedLanguageException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedLanguageException(UnsupportedLanguageException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LanguageNotFoundInListException.class)
    public ResponseEntity<ErrorResponse> handleLanguageNotFoundInListException(LanguageNotFoundInListException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(new ErrorResponse("Invalid value provided. Check enum fields."), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AvailabilityBlockNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAvailabilityBlockNotFound(AvailabilityBlockNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return new ResponseEntity<>(new ErrorResponse("An unexpected error occurred."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
