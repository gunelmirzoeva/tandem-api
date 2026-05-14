package com.example.tandem_api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordResponse {
    private String message;
    private String accessToken;
    private String refreshToken;
    private int expiresIn;
}
