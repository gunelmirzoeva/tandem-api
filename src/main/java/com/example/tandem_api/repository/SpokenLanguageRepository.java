package com.example.tandem_api.repository;

import com.example.tandem_api.domain.language.SpokenLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpokenLanguageRepository extends JpaRepository<SpokenLanguage, UUID> {
    List<SpokenLanguage> findByUserId(UUID userId);
    Optional<SpokenLanguage> findByUserIdAndLanguageCode(UUID userId, String languageCode);
    boolean existsByUserIdAndLanguageCode(UUID userId, String languageCode);
    long countByUserId(UUID userId);
}
