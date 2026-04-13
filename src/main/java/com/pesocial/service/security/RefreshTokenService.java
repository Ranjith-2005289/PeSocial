package com.pesocial.service.security;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.pesocial.model.security.RefreshToken;
import com.pesocial.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public RefreshToken create(String userId, String role, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setRole(role);
        refreshToken.setToken(token);
        refreshToken.setJti(jwtService.extractJti(token));
        refreshToken.setExpiresAt(jwtService.extractExpiry(token));
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateActiveToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token revoked or expired");
        }

        if (!jwtService.isTokenValid(token, "REFRESH")) {
            throw new IllegalArgumentException("Invalid refresh token type");
        }

        return refreshToken;
    }

    public void revokeByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    public void revokeAllForUser(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
