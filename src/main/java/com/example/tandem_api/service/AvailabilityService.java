package com.example.tandem_api.service;

import com.example.tandem_api.domain.availability.AvailabilityBlock;
import com.example.tandem_api.domain.user.User;
import com.example.tandem_api.dto.availability.AddAvailabilityBlockRequest;
import com.example.tandem_api.dto.availability.AvailabilityBlockResponse;
import com.example.tandem_api.dto.availability.AvailabilityProfileResponse;
import com.example.tandem_api.dto.availability.RemoveAvailabilityBlockResponse;
import com.example.tandem_api.exception.*;
import com.example.tandem_api.repository.AvailabilityBlockRepository;
import com.example.tandem_api.repository.UserRepository;
import com.example.tandem_api.util.TimeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
    private final AvailabilityBlockRepository availabilityBlockRepository;
    private final UserRepository userRepository;
    private final TimeConverter timeConverter;

    public AvailabilityProfileResponse getAvailability(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        ZoneId zoneId = ZoneId.of(user.getTimezone());

        List<AvailabilityBlock> blocks = availabilityBlockRepository.findByUserId(userId);

        List<AvailabilityBlockResponse> blockResponses = blocks.stream()
                .map(block -> AvailabilityBlockResponse.builder()
                        .blockId(block.getId())
                        .dayOfWeek(block.getDayOfWeek())
                        .startTimeLocal(timeConverter.utcToLocal(block.getStartTimeUtc(), zoneId))
                        .endTimeLocal(timeConverter.utcToLocal(block.getEndTimeUtc(), zoneId))
                        .startTimeUtc(block.getStartTimeUtc())
                        .endTimeUtc(block.getEndTimeUtc())
                        .build())
                .toList();

        return AvailabilityProfileResponse.builder()
                .timeZone(user.getTimezone())
                .blocks(blockResponses)
                .build();
    }

    public AvailabilityBlockResponse addBlock(UUID userId, AddAvailabilityBlockRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if(user.getTimezone() == null || user.getTimezone().trim().isEmpty()) {
            throw new TimezoneNotSetException("No timezone set on user profile");
        }

        ZoneId zoneId = ZoneId.of(user.getTimezone());

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new InvalidTimeRangeException("Start time after or equal to end time");
        }

        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
        if (duration.toMinutes() < 30) {
            throw new BlockDurationTooShortException("Block duration less than 30 minutes");
        }

        LocalTime startUtc = timeConverter.localToUTC(request.getStartTime(), zoneId);
        LocalTime endUtc = timeConverter.localToUTC(request.getEndTime(), zoneId);

        long existingBlockCount = availabilityBlockRepository.countByUserIdAndDayOfWeek(userId, request.getDayOfWeek());
        if (existingBlockCount >= 3) {
            throw new MaxBlocksPerDayExceededException("Max 3 blocks per day exceeded");
        }

        List<AvailabilityBlock> blocksOnSameDay = availabilityBlockRepository
                .findByUserIdAndDayOfWeek(userId, request.getDayOfWeek());

        for (AvailabilityBlock existing : blocksOnSameDay) {
            if (startUtc.isBefore(existing.getEndTimeUtc()) && endUtc.isAfter(existing.getStartTimeUtc())) {
                throw new AvailabilityBlockOverlapException("Overlap with existing block on same day");
            }
        }

        AvailabilityBlock block = AvailabilityBlock.builder()
                .user(user)
                .dayOfWeek(request.getDayOfWeek())
                .startTimeUtc(startUtc)
                .endTimeUtc(endUtc)
                .build();

        AvailabilityBlock saved = availabilityBlockRepository.save(block);

        return AvailabilityBlockResponse.builder()
                .blockId(saved.getId())
                .dayOfWeek(saved.getDayOfWeek())
                .startTimeLocal(request.getStartTime())
                .endTimeLocal(request.getEndTime())
                .startTimeUtc(startUtc)
                .endTimeUtc(endUtc)
                .build();

    }

    public RemoveAvailabilityBlockResponse removeBlock(UUID blockId, UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));


        AvailabilityBlock block = availabilityBlockRepository.findByIdAndUserId(blockId, userId)
                .orElseThrow(() -> new AvailabilityBlockNotFoundException("Block not found or belongs to another user"));

        long totalBlocks = availabilityBlockRepository.countByUserId(userId);
        if(totalBlocks == 1) {
            throw new CannotRemoveLastBlockException("Deleting last remaining block");
        }

        availabilityBlockRepository.delete(block);

        return RemoveAvailabilityBlockResponse.builder()
                .message("Availability block removed successfully.")
                .build();
    }

}
