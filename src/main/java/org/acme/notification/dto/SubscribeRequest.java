package org.acme.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record SubscribeRequest(@NotBlank String token) {
}
