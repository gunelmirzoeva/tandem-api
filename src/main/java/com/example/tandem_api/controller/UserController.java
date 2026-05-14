package com.example.tandem_api.controller;

import com.example.tandem_api.dto.auth.ChangePasswordRequest;
import com.example.tandem_api.dto.auth.ChangePasswordResponse;
import com.example.tandem_api.dto.user.UpdateProfileRequest;
import com.example.tandem_api.dto.user.UserProfileResponse;
import com.example.tandem_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile(getCurrentUserId()));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(getCurrentUserId(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(userService.changePassword(getCurrentUserId(), request));
    }


    private UUID getCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getName());
    }

}
