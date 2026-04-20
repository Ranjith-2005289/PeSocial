package com.pesocial.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.pesocial.dto.notification.CreateNotificationRequest;
import com.pesocial.factory.NotificationFactory;
import com.pesocial.model.notification.Notification;
import com.pesocial.model.notification.NotificationType;
import com.pesocial.model.system.SystemService;
import com.pesocial.repository.NotificationRepository;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final SystemService systemService;
    private final NotificationFactory notificationFactory;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   SimpMessagingTemplate messagingTemplate,
                                   SystemService systemService,
                                   NotificationFactory notificationFactory) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.systemService = systemService;
        this.notificationFactory = notificationFactory;
    }

    @Override
    public Notification sendNotification(CreateNotificationRequest request) {
        Notification notification = notificationFactory.createNotification(
            request.recipientId(),
            request.senderHandle(),
            request.type()
        );
        return saveNotification(notification, true);
    }

    @Override
    public Notification sendFollowNotification(String recipientId, String senderHandle) {
        // Duplicate prevention: Check for recent follow notification from same sender
        List<Notification> recentFollowNotifications = notificationRepository.findRecentNotifications(
            recipientId, senderHandle, NotificationType.FOLLOW, Instant.now().minusSeconds(3600)
        );
        
        if (!recentFollowNotifications.isEmpty()) {
            // Already notified of this follow recently, skip duplicate
            return recentFollowNotifications.get(0);
        }

        return saveNotification(notificationFactory.createFollowNotification(recipientId, senderHandle), true);
    }

    @Override
    public Notification sendLikeNotification(String recipientId, String senderHandle, String postId) {
        // Duplicate prevention: Check for recent like notification from same sender on same post
        List<Notification> recentLikeNotifications = notificationRepository.findRecentNotificationsWithPost(
            recipientId, senderHandle, NotificationType.LIKE, postId, Instant.now().minusSeconds(300)
        );
        
        if (!recentLikeNotifications.isEmpty()) {
            // Already notified of this like recently, skip duplicate
            return recentLikeNotifications.get(0);
        }

        Notification notification = notificationFactory.createLikeNotification(recipientId, senderHandle, postId);
        return saveNotification(notification, false);
    }

    @Override
    public Notification sendCommentNotification(String recipientId, String senderHandle, String postId, String commentText) {
        Notification notification = notificationFactory.createCommentNotification(recipientId, senderHandle, postId, commentText);
        return saveNotification(notification, false);
    }

    @Override
    public Notification sendMessageNotification(String recipientId, String senderHandle) {
        // Duplicate prevention: avoid spamming repeated message notifications in a short window
        List<Notification> recentMessageNotifications = notificationRepository.findRecentNotifications(
            recipientId, senderHandle, NotificationType.MESSAGE, Instant.now().minusSeconds(30)
        );

        if (!recentMessageNotifications.isEmpty()) {
            return recentMessageNotifications.get(0);
        }

        return saveNotification(notificationFactory.createMessageNotification(recipientId, senderHandle), true);
    }

    @Override
    public Notification markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> markAllAsRead(String recipientId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndIsReadFalse(recipientId);
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
        return notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public List<Notification> getNotifications(String recipientId) {
        return notificationRepository.findByRecipientIdOrderByTimestampDesc(recipientId);
    }

    @Override
    public long countUnread(String recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    private Notification saveNotification(Notification notification, boolean updateRecipientState) {
        Notification saved = notificationRepository.save(notification);

        if (updateRecipientState) {
            userRepository.findById(saved.getRecipientId()).ifPresent(recipient -> {
                recipient.receiveNotification(saved.getType());
                userRepository.save(recipient);
            });
            systemService.generateNotifications();
            systemService.logActivity();
        }

        pushRealtimeNotification(saved);
        return saved;
    }

    private void pushRealtimeNotification(Notification notification) {
        // Route by user handle (WebSocket connection is authenticated with handle)
        userRepository.findById(notification.getRecipientId()).ifPresent(recipient -> {
            String recipientHandle = recipient.getHandle();
            if (recipientHandle != null && !recipientHandle.isBlank()) {
                messagingTemplate.convertAndSendToUser(recipientHandle, "/queue/notifications", notification);
            }
        });

        // Fallback: Route by user ID (Spring Security will enforce authorization)
        messagingTemplate.convertAndSendToUser(notification.getRecipientId(), "/queue/notifications", notification);
    }
}
