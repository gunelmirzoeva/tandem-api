package com.example.tandem_api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyEmailRequest {
    @NotNull
    private UUID userId;

    @NotBlank(message = "OTP cannot be blank")
    @Size(min = 6, max = 6, message = "The code must be exactly 6 characters long")
    private String otp;
}
