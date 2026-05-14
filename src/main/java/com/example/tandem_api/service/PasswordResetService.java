package com.example.tandem_api.service;

import com.example.tandem_api.domain.user.Status;
import com.example.tandem_api.domain.user.User;
import com.example.tandem_api.dto.auth.ForgotPasswordRequest;
import com.example.tandem_api.dto.auth.ForgotPasswordResponse;
import com.example.tandem_api.dto.auth.ResetPasswordRequest;
import com.example.tandem_api.dto.auth.ResetPasswordResponse;
import com.example.tandem_api.exception.*;
import com.example.tandem_api.repository.UserRepository;
import com.example.tandem_api.util.OtpGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final OtpGenerator otpGenerator;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public ForgotPasswordResponse generateResetOtp(ForgotPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty() || userOptional.get().getStatus() != Status.ACTIVE) {
            return ForgotPasswordResponse.builder()
                    .message("If this email is registered you will receive a reset code shortly.")
                    .build();
        }

        User user = userOptional.get();
        checkRateLimit(request.getEmail());
        checkCooldown(user.getId());

        String otp = otpGenerator.generate();
        otpService.saveResetOtp(user.getId(), otp);
        otpService.incrementResetAttempts(request.getEmail());
        otpService.setResetCooldown(user.getId());
        emailService.sendOtpEmail(user.getEmail(), otp);

        return ForgotPasswordResponse.builder()
                .message("If this email is registered you will receive a reset code shortly.")
                .build();
    }

    public void checkRateLimit(String email) {
        int attempts = otpService.getResetAttempts(email);
        if (attempts >= 3) {
            throw new PasswordResetRateLimitException("Too many reset attempts. Try again later.");
        }
    }

    public void checkCooldown(UUID userId) {
        if (otpService.isResetCooldownActive(userId)) {
            throw new ResendCooldownActiveException("Please wait 2 minutes before requesting a new code.");
        }
    }

    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Optional<String> storedOtp = otpService.getResetOtp(user.getId());
        if (storedOtp.isEmpty()) {
            throw new OtpExpiredException("OTP expired");
        }

        if (!storedOtp.get().equals(request.getOtp())) {
            throw new OtpInvalidException("OTP incorrect");
        }

        otpService.deleteResetOtp(user.getId(), request.getEmail());

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenService.deleteAll(user.getId());

        return ResetPasswordResponse.builder()
                .message("Password reset successfully. Please log in.")
                .build();
    }
}
