package com.example.tandem_api.repository;

import com.example.tandem_api.domain.language.SupportedLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportedLanguageRepository extends JpaRepository<SupportedLanguage, String> {
    boolean existsByLanguageCode(String languageCode);
}
