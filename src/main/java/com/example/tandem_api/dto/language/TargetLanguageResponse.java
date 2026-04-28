package com.example.tandem_api.dto.language;

import com.example.tandem_api.domain.language.TargetProficiencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TargetLanguageResponse {
    private String languageCode;
    private String languageName;
    private TargetProficiencyLevel currentLevel;
}
