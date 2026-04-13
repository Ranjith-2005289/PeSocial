package com.pesocial.dto.user;

import java.util.List;

import com.pesocial.dto.post.PostResponseDto;

public record MyProfileDto(
    String id,
    String displayName,
    String handle,
    String email,
    String profilePhoto,
    String bio,
    String role,
    int followersCount,
    int followingCount,
    List<PostResponseDto> posts
) {
}
