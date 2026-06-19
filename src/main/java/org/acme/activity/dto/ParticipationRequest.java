package org.acme.activity.dto;

import jakarta.validation.constraints.NotNull;

public record ParticipationRequest(@NotNull boolean accept) {
}
