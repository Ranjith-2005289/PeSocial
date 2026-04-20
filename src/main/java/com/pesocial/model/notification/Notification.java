package com.pesocial.model.notification;

import java.time.Instant;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
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

    private Notification(Builder builder) {
        this.id = builder.id;
        this.recipientId = builder.recipientId;
        this.senderHandle = builder.senderHandle;
        this.type = builder.type;
        this.postId = builder.postId;
        this.isRead = builder.isRead;
        this.commentText = builder.commentText;
        this.timestamp = builder.timestamp;
    }

    public static Builder builder(String recipientId, String senderHandle, NotificationType type) {
        return new Builder(recipientId, senderHandle, type);
    }

    public static Notification follow(String recipientId, String senderHandle) {
        return builder(recipientId, senderHandle, NotificationType.FOLLOW).build();
    }

    public static Notification message(String recipientId, String senderHandle) {
        return builder(recipientId, senderHandle, NotificationType.MESSAGE).build();
    }

    public static Notification like(String recipientId, String senderHandle, String postId) {
        return builder(recipientId, senderHandle, NotificationType.LIKE)
            .postId(postId)
            .build();
    }

    public static Notification comment(String recipientId, String senderHandle, String postId, String commentText) {
        return builder(recipientId, senderHandle, NotificationType.COMMENT)
            .postId(postId)
            .commentText(commentText)
            .build();
    }

    public static final class Builder {
        private String id;
        private final String recipientId;
        private final String senderHandle;
        private final NotificationType type;
        private String postId;
        private boolean isRead;
        private String commentText;
        private Instant timestamp = Instant.now();

        private Builder(String recipientId, String senderHandle, NotificationType type) {
            this.recipientId = normalizeRequired("recipientId", recipientId);
            this.senderHandle = normalizeRequired("senderHandle", senderHandle);
            this.type = Objects.requireNonNull(type, "type is required");
        }

        public Builder id(String id) {
            this.id = normalizeOptional(id);
            return this;
        }

        public Builder postId(String postId) {
            this.postId = normalizeOptional(postId);
            return this;
        }

        public Builder commentText(String commentText) {
            this.commentText = normalizeOptional(commentText);
            return this;
        }

        public Builder isRead(boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = Objects.requireNonNull(timestamp, "timestamp is required");
            return this;
        }

        public Notification build() {
            validateConsistency();
            return new Notification(this);
        }

        private void validateConsistency() {
            if ((type == NotificationType.LIKE || type == NotificationType.COMMENT) && (postId == null || postId.isBlank())) {
                throw new IllegalArgumentException("postId is required for " + type.name() + " notifications");
            }
        }

        private static String normalizeRequired(String fieldName, String value) {
            String normalized = normalizeOptional(value);
            if (normalized == null) {
                throw new IllegalArgumentException(fieldName + " is required");
            }
            return normalized;
        }

        private static String normalizeOptional(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void deleteNotification() {
        this.isRead = true;
    }
}
