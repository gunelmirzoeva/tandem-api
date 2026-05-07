package com.example.tandem_api.dto.availability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityProfileResponse {
    private String timeZone;
    private List<AvailabilityBlockResponse> blocks;
}
