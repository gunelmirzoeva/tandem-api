package com.example.tandem_api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenPairResponse {

    private String accessToken;
    private String refreshToken;
    private int expiresIn;
}
