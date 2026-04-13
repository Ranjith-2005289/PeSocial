package com.pesocial.dto.message;

import java.time.Instant;

public record MessageDTO(
    String messageId,
    String chatRoomId,
    String senderId,
    String receiverId,
    String messageText,
    Instant timestamp,
    String messageStatus,
    Boolean isRead,
    String reaction,
    String attachmentUrl,
    String attachmentType
) {}
