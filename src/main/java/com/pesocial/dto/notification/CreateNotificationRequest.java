package com.pesocial.dto.notification;

import com.pesocial.model.notification.NotificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNotificationRequest(
    @NotBlank String recipientId,
    @NotBlank String senderHandle,
    @NotNull NotificationType type
) {
}
