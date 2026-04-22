package com.example.tandem_api.dto.user;

import com.example.tandem_api.domain.user.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {

    private UUID userId;
    private String fullName;
    private String email;
    private String timezone;
    private Status status;
    private boolean matchReady;
    private LocalDateTime createdAt;
}
