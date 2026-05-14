package com.example.tandem_api.service;

import com.example.tandem_api.domain.user.User;
import com.example.tandem_api.dto.auth.ChangePasswordRequest;
import com.example.tandem_api.dto.auth.ChangePasswordResponse;
import com.example.tandem_api.dto.user.UpdateProfileRequest;
import com.example.tandem_api.dto.user.UserProfileResponse;
import com.example.tandem_api.exception.*;
import com.example.tandem_api.repository.AvailabilityBlockRepository;
import com.example.tandem_api.repository.SpokenLanguageRepository;
import com.example.tandem_api.repository.TargetLanguageRepository;
import com.example.tandem_api.repository.UserRepository;
import com.example.tandem_api.util.TimezoneValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TimezoneValidator timezoneValidator;
    private final SpokenLanguageRepository spokenLanguageRepository;
    private final TargetLanguageRepository targetLanguageRepository;
    private final AvailabilityBlockRepository availabilityBlockRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserProfileResponse getMyProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserProfileResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .timezone(user.getTimezone())
                .status(user.getStatus())
                .matchReady(computeMatchReady(user.getId()))
                .createdAt(user.getCreatedAt())
                .build();

    }

    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!timezoneValidator.isValidTimezone(request.getTimezone())) {
            throw new InvalidTimezoneException("Invalid timezone");
        }

        user.setFullName(request.getFullName());
        user.setTimezone(request.getTimezone());

        userRepository.save(user);

        return UserProfileResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .timezone(user.getTimezone())
                .status(user.getStatus())
                .matchReady(computeMatchReady(user.getId()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    public boolean computeMatchReady(UUID userId) {
        long spokenCount = spokenLanguageRepository.countByUserId(userId);
        long targetCount = targetLanguageRepository.countByUserId(userId);
        long availabilityCount = availabilityBlockRepository.countByUserId(userId);
        return spokenCount > 0 && targetCount > 0 && availabilityCount > 0;
    }

    @Transactional
    public ChangePasswordResponse changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new CurrentPasswordIncorrectException("Current password incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new PasswordSameAsCurrentException("New password same as current");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenService.deleteAll(user.getId());

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        JwtService.RefreshTokenResult refreshToken = jwtService.generateRefreshToken(user.getId());
        refreshTokenService.store(user.getId(), refreshToken.tokenId());

        return ChangePasswordResponse.builder()
                .message("Password changed successfully.")
                .accessToken(accessToken)
                .refreshToken(refreshToken.token())
                .expiresIn(900)
                .build();
    }
}
