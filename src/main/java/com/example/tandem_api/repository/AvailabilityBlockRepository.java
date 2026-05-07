package com.example.tandem_api.repository;

import com.example.tandem_api.domain.availability.AvailabilityBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilityBlockRepository extends JpaRepository<AvailabilityBlock, UUID> {

    List<AvailabilityBlock> findByUserId(UUID userId);
    List<AvailabilityBlock> findByUserIdAndDayOfWeek(UUID userId, DayOfWeek dayOfWeek);
    long countByUserIdAndDayOfWeek(UUID userId, DayOfWeek dayOfWeek);
    long countByUserId(UUID userId);
    Optional<AvailabilityBlock> findByIdAndUserId(UUID id, UUID userId);
}