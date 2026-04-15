package com.example.tandem_api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserListResponse {

    private List<UserProfileResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

}
