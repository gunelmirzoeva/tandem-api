package com.example.tandem_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResendOtpRequest {
    @NotNull
    private UUID userId;
}
