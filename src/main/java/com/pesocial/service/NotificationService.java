package com.pesocial.service;

import java.util.List;

import com.pesocial.dto.notification.CreateNotificationRequest;
import com.pesocial.model.notification.Notification;

public interface NotificationService {
    Notification sendNotification(CreateNotificationRequest request);
    Notification sendFollowNotification(String recipientId, String senderHandle);
    Notification sendLikeNotification(String recipientId, String senderHandle, String postId);
    Notification sendCommentNotification(String recipientId, String senderHandle, String postId, String commentText);
    Notification markAsRead(String notificationId);
    List<Notification> markAllAsRead(String recipientId);
    void deleteNotification(String notificationId);
    List<Notification> getNotifications(String recipientId);
    long countUnread(String recipientId);
}
