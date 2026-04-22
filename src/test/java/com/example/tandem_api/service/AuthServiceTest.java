package com.example.tandem_api.service;


import com.example.tandem_api.domain.user.Status;
import com.example.tandem_api.domain.user.User;
import com.example.tandem_api.dto.auth.*;
import com.example.tandem_api.exception.*;
import com.example.tandem_api.repository.UserRepository;
import com.example.tandem_api.util.OtpGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpGenerator otpGenerator;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldReturnResponse_whenValidRequest() {
        RegisterRequest request = new RegisterRequest("gunel@gmail.com", "Secret123", "Gunel Mirzoeva");

        when(userRepository.existsByEmail("gunel@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("hashed");
        when(otpGenerator.generate()).thenReturn("123456");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = authService.register(request);

        assertEquals("Registration successful. Check your email for verification code.", response.getMessage());
        assertNotNull(response.getUserId());

        verify(otpService).saveOtp(eq(savedUser.getId()), eq("123456"));
        verify(emailService).sendOtpEmail(eq("gunel@gmail.com"), eq("123456"));

    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("gunel@gmail.com", "Secret123", "Gunel Mirzoeva");
        when(userRepository.existsByEmail("gunel@gmail.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldNormalizeEmailToLowercase() {
        RegisterRequest request = new RegisterRequest("GUNEL@Gmail.com", "Secret123", "Gunel Mirzoeva");

        when(userRepository.existsByEmail("gunel@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(otpGenerator.generate()).thenReturn("123456");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.register(request);
        verify(userRepository).existsByEmail("gunel@gmail.com");
    }

    @Test
    void verifyEmail_shouldActivateUser_whenOtpIsCorrect() {
        UUID userId = UUID.randomUUID();
        VerifyEmailRequest request = new VerifyEmailRequest(userId, "123456");

        User user = new User();
        user.setId(userId);
        user.setStatus(Status.PENDING);

        when(otpService.getAttempts(userId)).thenReturn(0);
        when(otpService.getOtp(userId)).thenReturn(Optional.of("123456"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        VerifyEmailResponse response = authService.verifyEmail(request);

        assertEquals("Email verified successfully. You can now log in.", response.getMessage());
        assertEquals(Status.ACTIVE, user.getStatus());
        verify(userRepository).save(user);
        verify(otpService).deleteOtp(userId);
    }

    @Test
    void verifyEmail_shouldThrow_whenOtpExpired() {
        UUID userId = UUID.randomUUID();
        VerifyEmailRequest request = new VerifyEmailRequest(userId, "123456");

        when(otpService.getAttempts(userId)).thenReturn(0);
        when(otpService.getOtp(userId)).thenReturn(Optional.empty());

        assertThrows(OtpExpiredException.class, () -> authService.verifyEmail(request));
    }

    @Test
    void verifyEmail_shouldThrow_whenAlreadyInvalidatedAfter5Attempts() {
        UUID userId = UUID.randomUUID();
        VerifyEmailRequest request = new VerifyEmailRequest(userId, "123456");

        when(otpService.getAttempts(userId)).thenReturn(5);

        assertThrows(OtpInvalidatedAfterAttemptsException.class, () -> authService.verifyEmail(request));
        verify(otpService, never()).getOtp(any());
    }

    @Test
    void verifyEmail_shouldInvalidateOtp_whenWrongOtpFifthTime() {
        UUID userId = UUID.randomUUID();
        VerifyEmailRequest request = new VerifyEmailRequest(userId, "000000");

        when(otpService.getAttempts(userId)).thenReturn(0);
        when(otpService.getOtp(userId)).thenReturn(Optional.of("123456"));
        when(otpService.incrementAttempts(userId)).thenReturn(5);

        assertThrows(OtpInvalidatedAfterAttemptsException.class, () -> authService.verifyEmail(request));
        verify(otpService).invalidateOtp(userId);
    }

    @Test
    void verifyEmail_shouldThrowOtpInvalid_whenWrongOtpAndAttemptsRemaining() {
        UUID userId = UUID.randomUUID();
        VerifyEmailRequest request = new VerifyEmailRequest(userId, "000000");

        when(otpService.getAttempts(userId)).thenReturn(0);
        when(otpService.getOtp(userId)).thenReturn(Optional.of("123456"));
        when(otpService.incrementAttempts(userId)).thenReturn(2);

        OtpInvalidException ex = assertThrows(OtpInvalidException.class, () -> authService.verifyEmail(request));
        assertTrue(ex.getMessage().contains("3"));
    }


    @Test
    void resendOtp_shouldSendNewOtp_whenAllChecksPass() {
        UUID userId = UUID.randomUUID();
        ResendOtpRequest request = new ResendOtpRequest(userId);

        User user = new User();
        user.setId(userId);
        user.setEmail("gunel@example.com");

        when(otpService.isCooldownActive(userId)).thenReturn(false);
        when(otpService.getResendCount(userId)).thenReturn(0);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(otpGenerator.generate()).thenReturn("654321");

        ResendOtpResponse response = authService.resendOtp(request);

        assertEquals("New OTP sent.", response.getMessage());
        assertEquals(120, response.getCooldownSeconds());
        verify(otpService).saveOtp(userId, "654321");
        verify(otpService).setCooldown(userId);
        verify(emailService).sendOtpEmail("gunel@example.com", "654321");
    }

    @Test
    void resendOtp_shouldThrow_whenCooldownActive() {
        UUID userId = UUID.randomUUID();
        ResendOtpRequest request = new ResendOtpRequest(userId);

        when(otpService.isCooldownActive(userId)).thenReturn(true);

        assertThrows(ResendCooldownActiveException.class, () -> authService.resendOtp(request));
        verify(userRepository, never()).findById(any());
    }

    @Test
    void resendOtp_shouldThrow_whenResendLimitReached() {
        UUID userId = UUID.randomUUID();
        ResendOtpRequest request = new ResendOtpRequest(userId);

        when(otpService.isCooldownActive(userId)).thenReturn(false);
        when(otpService.getResendCount(userId)).thenReturn(3);

        assertThrows(ResendLimitReachedException.class, () -> authService.resendOtp(request));
        verify(userRepository, never()).findById(any());
    }
}
