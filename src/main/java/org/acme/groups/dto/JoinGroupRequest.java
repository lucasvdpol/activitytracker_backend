package org.acme.groups.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinGroupRequest(@NotBlank String joinCode) {
}
