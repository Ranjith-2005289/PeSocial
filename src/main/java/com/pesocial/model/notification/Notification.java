package com.pesocial.model.notification;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    @Field("recipient_id")
    private String recipientId;

    @Field("sender_handle")
    private String senderHandle;

    @Field("type")
    private NotificationType type;

    @Field("post_id")
    private String postId;

    @Field("is_read")
    private boolean isRead;

    @Field("comment_text")
    private String commentText;

    @Field("timestamp")
    private Instant timestamp = Instant.now();

    public void markAsRead() {
        this.isRead = true;
    }

    public void deleteNotification() {
        this.isRead = true;
    }
}
