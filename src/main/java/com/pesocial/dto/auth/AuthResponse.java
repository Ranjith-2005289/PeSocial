package com.pesocial.dto.auth;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long accessTokenExpiresInSeconds,
    String userId,
    String role,
    String handle
) {
}
