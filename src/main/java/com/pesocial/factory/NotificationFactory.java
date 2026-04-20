package com.pesocial.factory;

import org.springframework.stereotype.Component;

import com.pesocial.model.notification.Notification;
import com.pesocial.model.notification.NotificationType;

@Component
public class NotificationFactory {

    public Notification createNotification(String recipientId, String senderHandle, NotificationType type) {
        return Notification.builder(recipientId, senderHandle, type).build();
    }

    public Notification createFollowNotification(String recipientId, String senderHandle) {
        return createNotification(recipientId, senderHandle, NotificationType.FOLLOW);
    }

    public Notification createMessageNotification(String recipientId, String senderHandle) {
        return createNotification(recipientId, senderHandle, NotificationType.MESSAGE);
    }

    public Notification createLikeNotification(String recipientId, String senderHandle, String postId) {
        return Notification.builder(recipientId, senderHandle, NotificationType.LIKE)
            .postId(postId)
            .build();
    }

    public Notification createCommentNotification(String recipientId, String senderHandle, String postId, String commentText) {
        return Notification.builder(recipientId, senderHandle, NotificationType.COMMENT)
            .postId(postId)
            .commentText(commentText)
            .build();
    }
}
