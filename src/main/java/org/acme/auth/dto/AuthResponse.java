package org.acme.auth.dto;

public record AuthResponse(String accessToken, String refreshToken) {
}
