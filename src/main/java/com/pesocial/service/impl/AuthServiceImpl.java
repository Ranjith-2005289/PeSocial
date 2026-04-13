package com.pesocial.service.impl;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pesocial.dto.auth.AuthResponse;
import com.pesocial.dto.auth.LoginRequest;
import com.pesocial.dto.auth.LogoutRequest;
import com.pesocial.dto.auth.RefreshTokenRequest;
import com.pesocial.dto.auth.RegisterRequest;
import com.pesocial.model.security.RefreshToken;
import com.pesocial.model.user.RegularUser;
import com.pesocial.model.user.User;
import com.pesocial.model.user.UserRole;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.AuthService;
import com.pesocial.service.security.JwtService;
import com.pesocial.service.security.RefreshTokenService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Email already registered");
        }

        String normalizedHandle = normalizeHandle(request.handle());
        if (userRepository.existsByHandle(normalizedHandle)) {
            throw new IllegalArgumentException("Handle already taken");
        }

        RegularUser user = new RegularUser();
        user.setUsername(request.username());
        user.setHandle(normalizedHandle);
        user.setEmail(request.email().toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.REGULAR_USER);

        User saved = userRepository.save(user);
        return issueTokens(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return issueTokens(user);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenService.validateActiveToken(request.refreshToken());
        String userId = stored.getUserId();
        String role = stored.getRole();

        refreshTokenService.revokeByToken(request.refreshToken());

        String accessToken = jwtService.generateAccessToken(userId, role);
        String refreshToken = jwtService.generateRefreshToken(userId, role);
        refreshTokenService.create(userId, role, refreshToken);

        String handle = userRepository.findById(userId).map(User::getHandle).orElse(null);
        return new AuthResponse(accessToken, refreshToken, "Bearer", 900, userId, role, handle);
    }

    @Override
    public void logout(LogoutRequest request) {
        refreshTokenService.revokeByToken(request.refreshToken());
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getRole().name());
        refreshTokenService.create(user.getId(), user.getRole().name(), refreshToken);

        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            900,
            user.getId(),
            user.getRole().name(),
            user.getHandle()
        );
    }

    private String normalizeHandle(String handle) {
        String trimmed = handle == null ? "" : handle.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Handle is required");
        }
        return trimmed.startsWith("@") ? trimmed : "@" + trimmed;
    }
}
