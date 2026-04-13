package com.pesocial.dto.user;

public record UserProfileDto(
    String id,
    String displayName,
    String handle,
    String email,
    String profilePhoto,
    String bio,
    String role,
    int followersCount,
    int followingCount
) {
}
