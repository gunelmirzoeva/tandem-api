package com.example.tandem_api.service;

import com.example.tandem_api.domain.language.SpokenLanguage;
import com.example.tandem_api.domain.language.SupportedLanguage;
import com.example.tandem_api.domain.language.TargetLanguage;
import com.example.tandem_api.domain.user.User;
import com.example.tandem_api.dto.language.*;
import com.example.tandem_api.exception.*;
import com.example.tandem_api.repository.SpokenLanguageRepository;
import com.example.tandem_api.repository.SupportedLanguageRepository;
import com.example.tandem_api.repository.TargetLanguageRepository;
import com.example.tandem_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LanguageProfileService {
    private final SpokenLanguageRepository spokenLanguageRepository;
    private final SupportedLanguageRepository supportedLanguageRepository;
    private final TargetLanguageRepository targetLanguageRepository;
    private final UserRepository userRepository;

    public LanguageProfileResponse getLanguageProfile(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<SpokenLanguage> spokenEntities = spokenLanguageRepository.findByUserId(userId);
        List<TargetLanguage> targetEntities = targetLanguageRepository.findByUserId(userId);

        Set<String> allCodes = new HashSet<>();
        spokenEntities.forEach(s -> allCodes.add(s.getLanguageCode()));
        targetEntities.forEach(t -> allCodes.add(t.getLanguageCode()));

        Map<String, String> languageNames = supportedLanguageRepository.findAllById(allCodes)
                .stream()
                .collect(Collectors.toMap(
                        SupportedLanguage::getLanguageCode,
                        SupportedLanguage::getLanguageName
                ));

        List<SpokenLanguageResponse> spoken = spokenEntities.stream()
                .map(s -> SpokenLanguageResponse.builder()
                        .languageCode(s.getLanguageCode())
                        .languageName(languageNames.getOrDefault(s.getLanguageCode(), s.getLanguageCode()))
                        .proficiencyLevel(s.getProficiencyLevel())
                        .build())
                .toList();

        List<TargetLanguageResponse> target = targetEntities.stream()
                .map(t -> TargetLanguageResponse.builder()
                        .languageCode(t.getLanguageCode())
                        .languageName(languageNames.getOrDefault(t.getLanguageCode(), t.getLanguageCode()))
                        .currentLevel(t.getCurrentLevel())
                        .build())
                .toList();

        return LanguageProfileResponse.builder()
                .spoken(spoken)
                .target(target)
                .build();
    }

    @Transactional
    public SpokenLanguageResponse addSpokenLanguage(UUID userId, AddSpokenLanguageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String languageCode = request.getLanguageCode().toLowerCase();

        if (!supportedLanguageRepository.existsById(languageCode)) {
            throw new UnsupportedLanguageException("Unsupported language code");
        }

        if (spokenLanguageRepository.existsByUserIdAndLanguageCode(userId, languageCode)) {
            throw new LanguageAlreadyInSpokenListException("Language already in spoken list");
        }

        if (targetLanguageRepository.existsByUserIdAndLanguageCode(userId, languageCode)) {
            throw new LanguageExistsInOppositeListException("Language already exists in target list");
        }

        SpokenLanguage spoken = SpokenLanguage.builder()
                .user(user)
                .languageCode(languageCode)
                .proficiencyLevel(request.getProficiencyLevel())
                .build();

        spokenLanguageRepository.save(spoken);

        String languageName = supportedLanguageRepository.findById(languageCode)
                .map(SupportedLanguage::getLanguageName)
                .orElse(languageCode);

        return SpokenLanguageResponse.builder()
                .languageCode(spoken.getLanguageCode())
                .languageName(languageName)
                .proficiencyLevel(spoken.getProficiencyLevel())
                .build();
    }

    @Transactional
    public RemoveLanguageResponse removeSpokenLanguage(UUID userId, String languageCode) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String normalizedCode = languageCode.toLowerCase();

        SpokenLanguage spoken = spokenLanguageRepository
                .findByUserIdAndLanguageCode(userId, normalizedCode)
                .orElseThrow(() -> new LanguageNotFoundInListException("Language not found in spoken list"));

        if (spokenLanguageRepository.countByUserId(userId) <= 1) {
            throw new CannotRemoveLastSpokenLanguageException("Cannot remove the last spoken language");
        }

        spokenLanguageRepository.delete(spoken);

        return RemoveLanguageResponse.builder()
                .message("Spoken language removed successfully.")
                .build();
    }

    @Transactional
    public TargetLanguageResponse addTargetLanguage(UUID userId, AddTargetLanguageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String languageCode = request.getLanguageCode().toLowerCase();

        if (!supportedLanguageRepository.existsById(languageCode)) {
            throw new UnsupportedLanguageException("Unsupported language code");
        }

        if (targetLanguageRepository.existsByUserIdAndLanguageCode(userId, languageCode)) {
            throw new LanguageAlreadyInTargetListException("Language already in target list");
        }

        if (spokenLanguageRepository.existsByUserIdAndLanguageCode(userId, languageCode)) {
            throw new LanguageExistsInOppositeListException("Language already exists in spoken list");
        }

        TargetLanguage target = TargetLanguage.builder()
                .user(user)
                .languageCode(languageCode)
                .currentLevel(request.getCurrentLevel())
                .build();

        targetLanguageRepository.save(target);

        String languageName = supportedLanguageRepository.findById(languageCode)
                .map(SupportedLanguage::getLanguageName)
                .orElse(languageCode);

        return TargetLanguageResponse.builder()
                .languageCode(target.getLanguageCode())
                .languageName(languageName)
                .currentLevel(target.getCurrentLevel())
                .build();
    }

    @Transactional
    public RemoveLanguageResponse removeTargetLanguage(UUID userId, String languageCode) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String normalizedCode = languageCode.toLowerCase();

        TargetLanguage target = targetLanguageRepository
                .findByUserIdAndLanguageCode(userId, normalizedCode)
                .orElseThrow(() -> new LanguageNotFoundInListException("Language not found in target list"));

        targetLanguageRepository.delete(target);

        return RemoveLanguageResponse.builder()
                .message("Target language removed successfully.")
                .build();
    }
}