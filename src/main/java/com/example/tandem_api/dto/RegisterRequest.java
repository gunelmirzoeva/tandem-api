package com.example.tandem_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$",
            message = "Password must be at least 8 characters long, contains one uppercase letter and one digit"
    )
    private String password;

    @NotBlank
    private String fullName;
}
