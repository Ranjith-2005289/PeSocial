package com.pesocial.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank String username,
    @NotBlank String handle,
    @Email String email,
    @NotBlank String password
) {
}
