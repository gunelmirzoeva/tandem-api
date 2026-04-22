package com.example.tandem_api.service;

import com.example.tandem_api.domain.user.Status;
import com.example.tandem_api.domain.user.User;
import com.example.tandem_api.dto.auth.*;
import com.example.tandem_api.exception.*;
import com.example.tandem_api.repository.UserRepository;
import com.example.tandem_api.util.OtpGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final OtpGenerator otpGenerator;
    private final EmailService emailService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final int EXPIRES_IN = 900;

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

    public TokenPairResponse login(LoginRequest request) {


       User user = userRepository.findByEmail(request.getEmail())
               .orElseThrow(() -> new InvalidCredentialsException("Email not found or wrong password"));

       if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
           throw new InvalidCredentialsException("Email not found or wrong password");
       }

       if (user.getStatus() == Status.PENDING) {
           throw new AccountNotVerifiedException("Account not verified");
       }

       if (user.getStatus() == Status.DEACTIVATED) {
           throw new AccountDeactivatedException("Account is deactivated");
       }

       String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
       JwtService.RefreshTokenResult refreshToken = jwtService.generateRefreshToken(user.getId());

       refreshTokenService.store(user.getId(), refreshToken.tokenId());

       return TokenPairResponse.builder()
               .accessToken(accessToken)
               .refreshToken(refreshToken.token())
               .expiresIn(EXPIRES_IN)
               .build();
    }

    public TokenPairResponse refresh(RefreshTokenRequest request) {
        if (!jwtService.validateToken(request.getRefreshToken())) {
            throw new RefreshTokenNotFoundException("Refresh token is invalid or expired");
        }

        String userIdStr = jwtService.extractUserId(request.getRefreshToken());
        String tokenId = jwtService.extractTokenId(request.getRefreshToken());

        UUID userId = UUID.fromString(userIdStr);

        if(refreshTokenService.isRotated(tokenId)) {
            refreshTokenService.deleteAll(userId);
            throw new ReplayAttackDetectedException("Replay attack detected");
        }

        if(!refreshTokenService.exists(userId, tokenId)) {
            throw new RefreshTokenNotFoundException("Refresh token not found");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        String accessToken = jwtService.generateAccessToken(userId, user.getEmail(), user.getRole());
        JwtService.RefreshTokenResult refreshToken = jwtService.generateRefreshToken(userId);

        refreshTokenService.rotate(userId, tokenId, refreshToken.tokenId());

        return TokenPairResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.token())
                .expiresIn(EXPIRES_IN)
                .build();

    }

    public LogoutResponse logout(LogoutRequest request) {
        if(!jwtService.validateToken(request.getRefreshToken())) {
            throw new RefreshTokenNotFoundException("Refresh token is invalid or expired");
        }

        String userId = jwtService.extractUserId(request.getRefreshToken());
        String tokenId = jwtService.extractTokenId(request.getRefreshToken());

        if (refreshTokenService.isRotated(tokenId)) {
            refreshTokenService.deleteAll(UUID.fromString(userId));
            throw new ReplayAttackDetectedException("Replay attack detected");
        }

        if (!refreshTokenService.exists(UUID.fromString(userId), tokenId)) {
            throw new RefreshTokenNotFoundException("Refresh token not found");
        }

        refreshTokenService.delete(UUID.fromString(userId), tokenId);

        return LogoutResponse.builder()
                .message("Logged out successfully")
                .build();

    }
}
