package org.acme.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record SendNotificationRequest(
        @NotBlank String title,
        @NotBlank String body,
        Long userId) {
}
