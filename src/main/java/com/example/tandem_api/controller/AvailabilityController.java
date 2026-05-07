package com.example.tandem_api.controller;

import com.example.tandem_api.dto.availability.AddAvailabilityBlockRequest;
import com.example.tandem_api.dto.availability.AvailabilityBlockResponse;
import com.example.tandem_api.dto.availability.AvailabilityProfileResponse;
import com.example.tandem_api.dto.availability.RemoveAvailabilityBlockResponse;
import com.example.tandem_api.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/users/me/availability")
@RequiredArgsConstructor
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<AvailabilityProfileResponse> getAvailability() {
        return ResponseEntity.ok(availabilityService.getAvailability(getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<AvailabilityBlockResponse> addBlock(@Valid @RequestBody AddAvailabilityBlockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availabilityService.addBlock(getCurrentUserId(), request));
    }

    @DeleteMapping("/{blockId}")
    public ResponseEntity<RemoveAvailabilityBlockResponse> removeBlock(@PathVariable UUID blockId) {
        return ResponseEntity.ok(availabilityService.removeBlock(blockId, getCurrentUserId()));
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getName());
    }
}
