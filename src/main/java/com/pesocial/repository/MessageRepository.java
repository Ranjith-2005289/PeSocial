package com.pesocial.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pesocial.model.message.Message;
import org.springframework.data.mongodb.repository.Query;


public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByReceiverIdOrderByTimestampDesc(String receiverId);
    List<Message> findBySenderIdAndReceiverIdOrderByTimestampAsc(String senderId, String receiverId);

    /**
     * Find all messages in a chat room ordered by timestamp ascending
     */
    List<Message> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);

    /**
     * Find all messages in a chat room ordered by timestamp descending
     */
    List<Message> findByChatRoomIdOrderByTimestampDesc(String chatRoomId);

    /**
     * Find unread messages for a receiver in a specific chat room
     */
    @Query("{ 'chat_room_id': ?0, 'receiver_id': ?1, 'is_read': false }")
    List<Message> findUnreadInChatRoom(String chatRoomId, String receiverId);

    /**
     * Find all unread messages for a receiver
     */
    @Query("{ 'receiver_id': ?0, 'is_read': false }")
    List<Message> findAllUnread(String receiverId);

    /**
     * Count unread messages for a receiver in a specific chat room
     */
    @Query(value = "{ 'chat_room_id': ?0, 'receiver_id': ?1, 'is_read': false }", count = true)
    long countUnreadInChatRoom(String chatRoomId, String receiverId);

    /**
     * Count total unread messages for a receiver
     */
    @Query(value = "{ 'receiver_id': ?0, 'is_read': false }", count = true)
    long countAllUnread(String receiverId);
}
