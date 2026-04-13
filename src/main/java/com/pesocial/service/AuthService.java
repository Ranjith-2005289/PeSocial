package com.pesocial.service;

import com.pesocial.dto.auth.AuthResponse;
import com.pesocial.dto.auth.LoginRequest;
import com.pesocial.dto.auth.LogoutRequest;
import com.pesocial.dto.auth.RefreshTokenRequest;
import com.pesocial.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(LogoutRequest request);
}
