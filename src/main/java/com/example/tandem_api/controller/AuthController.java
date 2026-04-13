package com.example.tandem_api.controller;

import com.example.tandem_api.dto.*;
import com.example.tandem_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/auth/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        VerifyEmailResponse response = authService.verifyEmail(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/auth/resend-otp")
    public ResponseEntity<ResendOtpResponse> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        ResendOtpResponse response = authService.resendOtp(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
