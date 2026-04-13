package com.pesocial.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateMyProfileRequest(
    @NotBlank String username,
    @NotBlank String handle,
    String bio
) {
}
