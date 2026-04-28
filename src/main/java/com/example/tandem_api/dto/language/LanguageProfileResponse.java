package com.example.tandem_api.dto.language;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LanguageProfileResponse {
    private List<SpokenLanguageResponse> spoken;
    private List<TargetLanguageResponse> target;
}
