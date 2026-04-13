package com.pesocial.model.message;

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
@Document(collection = "messages")
public class Message {

    @Id
    private String messageId;

    @Field("chat_room_id")
    private String chatRoomId;

    @Field("sender_id")
    private String senderId;

    @Field("receiver_id")
    private String receiverId;

    @Field("message_text")
    private String messageText;

    @Field("timestamp")
    private Instant timestamp = Instant.now();

    @Field("message_status")
    private String messageStatus = "SENT";

    @Field("is_read")
    private Boolean isRead = false;

    @Field("reaction")
    private String reaction;

    @Field("attachment_url")
    private String attachmentUrl;

    @Field("attachment_type")
    private String attachmentType;

    public void markAsRead() {
        this.messageStatus = "READ";
        this.isRead = true;
    }

    public void addReaction(String emoji) {
        this.reaction = emoji;
    }

    public void removeReaction() {
        this.reaction = null;
    }
}

