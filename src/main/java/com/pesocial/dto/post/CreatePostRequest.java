package com.pesocial.dto.post;

import com.pesocial.model.post.MediaUrl;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
    @NotBlank String authorId,
    String contentText,
    MediaUrl media,
    String mediaType,
    String visibility
) {
}
