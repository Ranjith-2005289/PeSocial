package com.pesocial.dto.post;

import java.time.Instant;
import java.util.List;

import com.pesocial.model.post.MediaUrl;

public record PostResponseDto(
    String id,
    String authorId,
    String authorName,
    String contentText,
    MediaUrl media,
    String mediaId,
    String mediaType,
    String visibility,
    int likesCount,
    int sharesCount,
    List<String> comments,
    Instant createdAt,
    Instant updatedAt
) {
}
