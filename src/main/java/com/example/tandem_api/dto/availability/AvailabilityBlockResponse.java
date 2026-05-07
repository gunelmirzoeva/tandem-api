package com.example.tandem_api.dto.availability;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityBlockResponse {
    private UUID blockId;
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTimeLocal;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTimeLocal;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTimeUtc;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTimeUtc;
}
