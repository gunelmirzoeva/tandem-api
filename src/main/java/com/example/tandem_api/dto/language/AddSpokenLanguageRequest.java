package com.example.tandem_api.dto.language;

import com.example.tandem_api.domain.language.SpokenProficiencyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddSpokenLanguageRequest {

    @NotBlank(message = "Language code is required")
    private String languageCode;

    @NotNull(message = "Proficiency level is required")
    private SpokenProficiencyLevel proficiencyLevel;
}
