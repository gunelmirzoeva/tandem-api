package com.example.tandem_api.repository;

import com.example.tandem_api.domain.language.TargetLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TargetLanguageRepository extends JpaRepository<TargetLanguage, UUID> {
    List<TargetLanguage> findByUserId(UUID userId);
    boolean existsByUserIdAndLanguageCode(UUID userId, String languageCode);
    Optional<TargetLanguage> findByUserIdAndLanguageCode(UUID userId, String languageCode);
    long countByUserId(UUID userId);
}
