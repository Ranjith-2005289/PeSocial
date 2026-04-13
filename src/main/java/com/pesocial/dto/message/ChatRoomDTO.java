package com.pesocial.dto.message;

import java.time.Instant;

public record ChatRoomDTO(
    String chatRoomId,
    String otherUserId,
    String otherUserHandle,
    String otherUserUsername,
    String otherUserProfilePhoto,
    String lastMessageText,
    Instant lastMessageTimestamp,
    Long unreadCount,
    Boolean pinned,
    Boolean archived
) {}
