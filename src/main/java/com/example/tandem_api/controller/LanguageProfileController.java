package com.example.tandem_api.controller;

import com.example.tandem_api.dto.language.*;
import com.example.tandem_api.service.LanguageProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/users/me/language-profile")
@RequiredArgsConstructor
public class LanguageProfileController {
    private final LanguageProfileService languageProfileService;

    @GetMapping
    public ResponseEntity<LanguageProfileResponse> getLanguageProfile() {
        return ResponseEntity.ok(languageProfileService.getLanguageProfile(getCurrentUserId()));
    }

    @PostMapping("/spoken")
    public ResponseEntity<SpokenLanguageResponse> addSpokenLanguage(@Valid @RequestBody AddSpokenLanguageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(languageProfileService.addSpokenLanguage(getCurrentUserId(), request));
    }

    @DeleteMapping("/spoken/{languageCode}")
    public ResponseEntity<RemoveLanguageResponse> removeSpokenLanguage(@PathVariable String languageCode) {
        return ResponseEntity.ok(languageProfileService.removeSpokenLanguage(getCurrentUserId(), languageCode));
    }

    @PostMapping("/target")
    public ResponseEntity<TargetLanguageResponse> addTargetLanguage(@Valid @RequestBody AddTargetLanguageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(languageProfileService.addTargetLanguage(getCurrentUserId(), request));
    }

    @DeleteMapping("/target/{languageCode}")
    public ResponseEntity<RemoveLanguageResponse> removeTargetLanguage(@PathVariable String languageCode) {
        return ResponseEntity.ok(languageProfileService.removeTargetLanguage(getCurrentUserId(), languageCode));
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getName());
    }
}
