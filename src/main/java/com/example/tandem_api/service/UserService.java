package com.example.tandem_api.service;

import com.example.tandem_api.domain.user.User;
import com.example.tandem_api.dto.user.UpdateProfileRequest;
import com.example.tandem_api.dto.user.UserProfileResponse;
import com.example.tandem_api.exception.InvalidTimezoneException;
import com.example.tandem_api.exception.UserNotFoundException;
import com.example.tandem_api.repository.AvailabilityBlockRepository;
import com.example.tandem_api.repository.SpokenLanguageRepository;
import com.example.tandem_api.repository.TargetLanguageRepository;
import com.example.tandem_api.repository.UserRepository;
import com.example.tandem_api.util.TimezoneValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TimezoneValidator timezoneValidator;
    private final SpokenLanguageRepository spokenLanguageRepository;
    private final TargetLanguageRepository targetLanguageRepository;
    private final AvailabilityBlockRepository availabilityBlockRepository;

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

    private boolean computeMatchReady(UUID userId) {
        long spokenCount = spokenLanguageRepository.countByUserId(userId);
        long targetCount = targetLanguageRepository.countByUserId(userId);
        long availabilityCount = availabilityBlockRepository.countByUserId(userId);
        return spokenCount > 0 && targetCount > 0 && availabilityCount > 0;
    }
}
