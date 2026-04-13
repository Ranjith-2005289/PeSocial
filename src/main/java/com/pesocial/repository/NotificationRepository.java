package com.pesocial.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.pesocial.model.notification.Notification;
import com.pesocial.model.notification.NotificationType;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipientIdOrderByTimestampDesc(String recipientId);
    
    List<Notification> findByRecipientIdAndIsReadFalse(String recipientId);
    
    long countByRecipientIdAndIsReadFalse(String recipientId);
    
    @Query("{ 'recipient_id': ?0, 'sender_handle': ?1, 'type': ?2, 'timestamp': { $gte: ?3 } }")
    List<Notification> findRecentNotifications(String recipientId, String senderHandle, NotificationType type, Instant fromTimestamp);
    
    @Query("{ 'recipient_id': ?0, 'sender_handle': ?1, 'type': ?2, 'post_id': ?3, 'timestamp': { $gte: ?4 } }")
    List<Notification> findRecentNotificationsWithPost(String recipientId, String senderHandle, NotificationType type, String postId, Instant fromTimestamp);
}
