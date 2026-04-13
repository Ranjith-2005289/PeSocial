package com.pesocial.service;

import java.util.List;

import com.pesocial.dto.message.ChatRoomDTO;
import com.pesocial.dto.message.MessageDTO;
import com.pesocial.dto.user.UserSummaryDto;
import com.pesocial.model.message.ChatRoom;
import com.pesocial.model.message.Message;

public interface ChatRoomService {
    /**
     * Create a new chat room or get existing one between two users
     */
    ChatRoom createOrGetChatRoom(String userId1, String userId2);

    /**
     * Create a new chat room or get existing one by target user's handle
     */
    ChatRoomDTO createOrGetChatRoomByHandle(String currentUserId, String targetHandle);

    /**
     * Get chat room by ID
     */
    ChatRoom getChatRoom(String chatRoomId);

    /**
     * Get all chat rooms for a user (ordered by most recent)
     */
    List<ChatRoom> getAllChatRoomsForUser(String userId);

    /**
     * Get all chat rooms for a user with user details
     */
    List<ChatRoomDTO> getAllChatRoomsWithDetails(String userId);

    /**
     * Get messages in a chat room with pagination
     */
    List<Message> getChatHistory(String chatRoomId, int limit, int offset);

    /**
     * Get messages as DTOs in a chat room
     */
    List<MessageDTO> getChatHistoryDTO(String chatRoomId, int limit, int offset);

    /**
     * Mark all messages in chat room as read
     */
    void markChatRoomAsRead(String chatRoomId, String userId);

    /**
     * Archive a chat room
     */
    void archiveChatRoom(String chatRoomId);

    /**
     * Unarchive a chat room
     */
    void unarchiveChatRoom(String chatRoomId);

    /**
     * Pin a chat room
     */
    void pinChatRoom(String chatRoomId);

    /**
     * Unpin a chat room
     */
    void unpinChatRoom(String chatRoomId);

    /**
     * Search users for chat (returns users with profile photos and handles)
     */
    List<UserSummaryDto> searchUsersForChat(String currentUserId, String searchQuery);

    /**
     * Get total unread count for a user across all chat rooms
     */
    long getTotalUnreadCount(String userId);

    /**
     * Get unread count for a specific chat room
     */
    long getUnreadCountForChatRoom(String chatRoomId, String userId);

    /**
     * Delete a chat room
     */
    void deleteChatRoom(String chatRoomId);
}
