package com.example.tandem_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class VerifyEmailRequest {
    @NotNull
    private UUID userId;
    @NotBlank
    @Size(min = 6, max = 6, message = "The code must be exactly 6 characters long")
    private String otp;
}
