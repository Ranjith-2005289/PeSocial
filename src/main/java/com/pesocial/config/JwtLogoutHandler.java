package com.pesocial.config;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesocial.dto.auth.LogoutRequest;
import com.pesocial.service.security.JwtService;
import com.pesocial.service.security.RefreshTokenService;
import com.pesocial.service.security.TokenBlocklistService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtLogoutHandler implements LogoutHandler {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlocklistService tokenBlocklistService;
    private final ObjectMapper objectMapper;

    public JwtLogoutHandler(JwtService jwtService,
                            RefreshTokenService refreshTokenService,
                            TokenBlocklistService tokenBlocklistService,
                            ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.tokenBlocklistService = tokenBlocklistService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String accessToken = authHeader.substring(BEARER_PREFIX.length());
            try {
                String jti = jwtService.extractJti(accessToken);
                tokenBlocklistService.blacklist(jti, jwtService.extractExpiry(accessToken));
            } catch (Exception ignored) {
                // Ignore invalid access token during logout.
            }
        }

        try {
            LogoutRequest logoutRequest = objectMapper.readValue(request.getInputStream(), LogoutRequest.class);
            refreshTokenService.revokeByToken(logoutRequest.refreshToken());
        } catch (Exception ignored) {
            // Logout still succeeds even when refresh token is absent/invalid.
        }
    }
}
