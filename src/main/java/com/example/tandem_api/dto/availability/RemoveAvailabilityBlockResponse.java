package com.example.tandem_api.dto.availability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemoveAvailabilityBlockResponse {
    private String message;
}
