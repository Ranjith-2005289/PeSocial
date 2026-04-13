package com.pesocial.model.message;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ChatRoom entity to group messages between two users.
 * This optimizes fetching history and provides better organization.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_rooms")
public class ChatRoom {

    @Id
    private String chatRoomId;

    @Field("user1_id")
    private String user1Id;

    @Field("user2_id")
    private String user2Id;

    @Field("last_message_id")
    private String lastMessageId;

    @Field("last_message_text")
    private String lastMessageText;

    @Field("last_message_timestamp")
    private Instant lastMessageTimestamp;

    @Field("unread_count_user1")
    private Long unreadCountUser1 = 0L;

    @Field("unread_count_user2")
    private Long unreadCountUser2 = 0L;

    @Field("created_at")
    private Instant createdAt = Instant.now();

    @Field("updated_at")
    private Instant updatedAt = Instant.now();

    @Field("archived")
    private Boolean archived = false;

    @Field("pinned")
    private Boolean pinned = false;

    // Constructor for creating new chat room
    public ChatRoom(String user1Id, String user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.unreadCountUser1 = 0L;
        this.unreadCountUser2 = 0L;
    }

    /**
     * Update last message details and timestamp
     */
    public void updateLastMessage(String messageId, String messageText) {
        this.lastMessageId = messageId;
        this.lastMessageText = messageText;
        this.lastMessageTimestamp = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Get the other user's ID
     */
    public String getOtherUserId(String currentUserId) {
        return user1Id.equals(currentUserId) ? user2Id : user1Id;
    }

    /**
     * Increment unread count for specific user
     */
    public void incrementUnreadCount(String userId) {
        if (user1Id.equals(userId)) {
            this.unreadCountUser1 = (this.unreadCountUser1 != null ? this.unreadCountUser1 : 0L) + 1;
        } else if (user2Id.equals(userId)) {
            this.unreadCountUser2 = (this.unreadCountUser2 != null ? this.unreadCountUser2 : 0L) + 1;
        }
    }

    /**
     * Reset unread count for specific user
     */
    public void resetUnreadCount(String userId) {
        if (user1Id.equals(userId)) {
            this.unreadCountUser1 = 0L;
        } else if (user2Id.equals(userId)) {
            this.unreadCountUser2 = 0L;
        }
    }

    /**
     * Get unread count for specific user
     */
    public Long getUnreadCount(String userId) {
        if (user1Id.equals(userId)) {
            return this.unreadCountUser1 != null ? this.unreadCountUser1 : 0L;
        } else if (user2Id.equals(userId)) {
            return this.unreadCountUser2 != null ? this.unreadCountUser2 : 0L;
        }
        return 0L;
    }
}
