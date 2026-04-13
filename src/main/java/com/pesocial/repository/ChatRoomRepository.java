package com.pesocial.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.pesocial.model.message.ChatRoom;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    
    /**
     * Find chat room between two specific users
     */
    @Query("{ $or: [ " +
           "{ 'user1_id': ?0, 'user2_id': ?1 }, " +
           "{ 'user1_id': ?1, 'user2_id': ?0 } " +
           "] }")
    Optional<ChatRoom> findBetweenUsers(String user1Id, String user2Id);

    /**
     * Find all chat rooms for a user (ordered by most recent)
     */
    @Query("{ $or: [ { 'user1_id': ?0 }, { 'user2_id': ?0 } ], 'archived': false }")
    List<ChatRoom> findAllForUser(String userId);

    /**
     * Find all chat rooms for a user (ordered by last message timestamp, descending)
     */
    @Query(value = "{ $or: [ { 'user1_id': ?0 }, { 'user2_id': ?0 } ], 'archived': false }", 
           sort = "{ 'last_message_timestamp': -1 }")
    List<ChatRoom> findAllForUserOrderByRecent(String userId);

    /**
     * Find all unread chat rooms for a user
     */
    @Query("{ $or: [ " +
           "{ 'user1_id': ?0, 'unread_count_user1': { $gt: 0 } }, " +
           "{ 'user2_id': ?0, 'unread_count_user2': { $gt: 0 } } " +
           "] }")
    List<ChatRoom> findUnreadChatRooms(String userId);

    /**
     * Find pinned chat rooms for a user
     */
    @Query(value = "{ $or: [ { 'user1_id': ?0 }, { 'user2_id': ?0 } ], 'pinned': true }", 
           sort = "{ 'last_message_timestamp': -1 }")
    List<ChatRoom> findPinnedChatRooms(String userId);
}
