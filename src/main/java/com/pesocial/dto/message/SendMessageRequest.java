package com.pesocial.dto.message;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
    @NotBlank String senderId,
    @NotBlank String receiverId,
    @NotBlank String messageText,
    String chatRoomId,
    String attachmentUrl,
    String attachmentType
) {
}
