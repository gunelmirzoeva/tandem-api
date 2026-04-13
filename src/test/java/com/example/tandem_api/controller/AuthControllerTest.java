package com.example.tandem_api.controller;

import com.example.tandem_api.config.SecurityConfig;
import com.example.tandem_api.dto.*;
import com.example.tandem_api.exception.*;
import com.example.tandem_api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private final UUID userId = UUID.randomUUID();


    //POST auth/register

    @Test
    void register_shouldReturn201_whenValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("gunel@gmail.com", "Secret123", "Gunel Mirzoeva");
        RegisterResponse response = RegisterResponse.builder()
                .message("Registration successful. Check your email for verification code.")
                .userId(userId)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful. Check your email for verification code."))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest("Gunel Mammadova", "notanemail", "Secret123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenPasswordIsWeak() throws Exception {
        RegisterRequest request = new RegisterRequest("Gunel Mammadova", "gunel@example.com", "weakpass");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void register_shouldReturn400_whenFieldsAreBlank() throws Exception {
        RegisterRequest request = new RegisterRequest("", "", "");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("gunel@example.com", "Secret123", "Gunel Mirzoeva");

        when(authService.register(any())).thenThrow(new EmailAlreadyExistsException("Email already registered"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }
    @Test
    void register_shouldReturn500_whenUnexpectedErrorOccurs() throws Exception {
        RegisterRequest request = new RegisterRequest("gunel@example.com", "Secret123", "Gunel Mirzoeva");

        when(authService.register(any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."));
    }



    //POST /auth/verify-email
    @Test
    void verifyEmail_shouldReturn200_whenOtpIsCorrect() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest(userId, "123456");
        VerifyEmailResponse response = VerifyEmailResponse.builder()
                .message("Email verified successfully. You can now log in.")
                .build();

        when(authService.verifyEmail(any())).thenReturn(response);

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully. You can now log in."));
    }

    @Test
    void verifyEmail_shouldReturn410_whenOtpExpired() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest(userId, "123456");

        when(authService.verifyEmail(any())).thenThrow(new OtpExpiredException("OTP expired or not found"));

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value("OTP expired or not found"));
    }

    @Test
    void verifyEmail_shouldReturn400_whenOtpIsWrong() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest(userId, "000000");

        when(authService.verifyEmail(any())).thenThrow(new OtpInvalidException("OTP wrong, attempts remaining: 4"));

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("OTP wrong, attempts remaining: 4"));
    }

    @Test
    void verifyEmail_shouldReturn404_whenUserNotFound() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest(userId, "123456");

        when(authService.verifyEmail(any())).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
    @Test
    void verifyEmail_shouldReturn423_whenOtpInvalidatedAfterAttempts() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest(userId, "000000");

        when(authService.verifyEmail(any())).thenThrow(new OtpInvalidatedAfterAttemptsException("OTP invalidated after 5 attempts"));

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.message").value("OTP invalidated after 5 attempts"));
    }

    //POST /auth/resend-otp

    @Test
    void resendOtp_shouldReturn200_whenAllChecksPass() throws Exception {
        ResendOtpRequest request = new ResendOtpRequest(userId);
        ResendOtpResponse response = ResendOtpResponse.builder()
                .message("New OTP sent.")
                .cooldownSeconds(120)
                .build();

        when(authService.resendOtp(any())).thenReturn(response);

        mockMvc.perform(post("/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New OTP sent."))
                .andExpect(jsonPath("$.cooldownSeconds").value(120));
    }

    @Test
    void resendOtp_shouldReturn429_whenCooldownActive() throws Exception {
        ResendOtpRequest request = new ResendOtpRequest(userId);

        when(authService.resendOtp(any())).thenThrow(new ResendCooldownActiveException("Resend cooldown active"));

        mockMvc.perform(post("/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Resend cooldown active"));
    }

    @Test
    void resendOtp_shouldReturn429_whenResendLimitReached() throws Exception {
        ResendOtpRequest request = new ResendOtpRequest(userId);

        when(authService.resendOtp(any())).thenThrow(new ResendLimitReachedException("Resend limit reached"));

        mockMvc.perform(post("/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Resend limit reached"));
    }

}


