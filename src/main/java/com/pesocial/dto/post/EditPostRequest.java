package com.pesocial.dto.post;

import com.pesocial.model.post.MediaUrl;

public record EditPostRequest(
    String contentText,
    MediaUrl media,
    String mediaType,
    String visibility
) {
}
