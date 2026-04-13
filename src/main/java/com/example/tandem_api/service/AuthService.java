package com.example.tandem_api.service;

import com.example.tandem_api.domain.Status;
import com.example.tandem_api.domain.User;
import com.example.tandem_api.dto.*;
import com.example.tandem_api.exception.*;
import com.example.tandem_api.repository.UserRepository;
import com.example.tandem_api.util.OtpGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final OtpGenerator otpGenerator;
    private final EmailService emailService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        String otp = otpGenerator.generate();
        otpService.saveOtp(savedUser.getId(), otp);
        emailService.sendOtpEmail(request.getEmail(), otp);

        return RegisterResponse.builder()
                .message("Registration successful. Check your email for verification code.")
                .userId(savedUser.getId())
                .build();
    }

    @Transactional
    public VerifyEmailResponse verifyEmail(VerifyEmailRequest request) {

        if(otpService.getAttempts(request.getUserId()) >= 5){
            throw new OtpInvalidatedAfterAttemptsException("OTP invalidated after 5 attempts");
        }

        Optional<String> storedOtp = otpService.getOtp(request.getUserId());

        if (storedOtp.isEmpty()) {
            throw new OtpExpiredException("OTP expired or not found");
        }

        if (!storedOtp.get().equals(request.getOtp())) {

            int attempts = otpService.incrementAttempts(request.getUserId());
            if (attempts >= 5) {
                otpService.invalidateOtp(request.getUserId());
                throw new OtpInvalidatedAfterAttemptsException("OTP invalidated after 5 attempts");
            }
            int remainingAttempts = 5 - attempts;
            throw new OtpInvalidException("OTP wrong, attempts remaining: " + remainingAttempts);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));

        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        otpService.deleteOtp(request.getUserId());

        return VerifyEmailResponse.builder()
                .message("Email verified successfully. You can now log in.")
                .build();
    }

    public ResendOtpResponse resendOtp(ResendOtpRequest request) {
        if (otpService.isCooldownActive(request.getUserId())) {
            throw new ResendCooldownActiveException("Resend cooldown active");
        }
        if (otpService.getResendCount(request.getUserId()) >= 3) {
            throw new ResendLimitReachedException("Resend limit reached");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));

        otpService.saveResendCount(request.getUserId());

        String otp = otpGenerator.generate();
        otpService.saveOtp(request.getUserId(), otp);
        otpService.setCooldown(request.getUserId());
        emailService.sendOtpEmail(user.getEmail(), otp);

        return ResendOtpResponse.builder()
                .message("New OTP sent.")
                .cooldownSeconds(120)
                .build();
    }
}
