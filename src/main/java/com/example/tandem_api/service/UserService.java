package com.example.tandem_api.service;

import com.example.tandem_api.domain.User;
import com.example.tandem_api.dto.user.UpdateProfileRequest;
import com.example.tandem_api.dto.user.UserProfileResponse;
import com.example.tandem_api.exception.InvalidTimezoneException;
import com.example.tandem_api.exception.UserNotFoundException;
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

    public UserProfileResponse getMyProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserProfileResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .timezone(user.getTimezone())
                .status(user.getStatus())
                .matchReady(false) //for now
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
                .matchReady(false) // for now
                .createdAt(user.getCreatedAt())
                .build();
    }
}
