package com.example.tandem_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ResendOtpRequest {
    @NotNull
    private UUID userId;
}
