package org.acme.activity.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record ActivityRequest(
        @NotBlank String name,
        String location,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<Long> invitedUserIds) {
}
