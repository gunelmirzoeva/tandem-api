package com.example.tandem_api.dto.language;

import com.example.tandem_api.domain.language.TargetProficiencyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddTargetLanguageRequest {

    @NotBlank(message = "Language code is required")
    private String languageCode;

    @NotNull(message = "Current level is required")
    private TargetProficiencyLevel currentLevel;
}
