package com.pesocial.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pesocial.dto.message.ChatRoomDTO;
import com.pesocial.dto.message.MessageDTO;
import com.pesocial.dto.user.UserSummaryDto;
import com.pesocial.model.message.ChatRoom;
import com.pesocial.service.ChatRoomService;

@RestController
@RequestMapping("/api/chat-rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    /**
     * Create or get chat room between current user and another user
     */
    @PostMapping("/{otherUserId}")
    public ResponseEntity<ChatRoom> createOrGetChatRoom(@PathVariable String otherUserId) {
        String currentUserId = getCurrentUserId();
        ChatRoom chatRoom = chatRoomService.createOrGetChatRoom(currentUserId, otherUserId);
        return ResponseEntity.ok(chatRoom);
    }

    /**
     * Create or get chat room by target user's handle
     */
    @GetMapping("/room/{targetHandle}")
    public ResponseEntity<ChatRoomDTO> createOrGetChatRoomByHandle(@PathVariable String targetHandle) {
        String currentUserId = getCurrentUserId();
        ChatRoomDTO chatRoom = chatRoomService.createOrGetChatRoomByHandle(currentUserId, targetHandle);
        return ResponseEntity.ok(chatRoom);
    }

    /**
     * Get all chat rooms for current user (ordered by most recent)
     */
    @GetMapping
    public ResponseEntity<List<ChatRoomDTO>> getAllChatRooms() {
        String currentUserId = getCurrentUserId();
        List<ChatRoomDTO> chatRooms = chatRoomService.getAllChatRoomsWithDetails(currentUserId);
        return ResponseEntity.ok(chatRooms);
    }

    /**
     * Get messages in a specific chat room with pagination
     */
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<List<MessageDTO>> getChatHistory(
            @PathVariable String chatRoomId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        List<MessageDTO> messages = chatRoomService.getChatHistoryDTO(chatRoomId, limit, offset);
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark all messages in a chat room as read
     */
    @PatchMapping("/{chatRoomId}/mark-as-read")
    public ResponseEntity<Void> markChatRoomAsRead(@PathVariable String chatRoomId) {
        String currentUserId = getCurrentUserId();
        chatRoomService.markChatRoomAsRead(chatRoomId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Archive a chat room
     */
    @PatchMapping("/{chatRoomId}/archive")
    public ResponseEntity<Void> archiveChatRoom(@PathVariable String chatRoomId) {
        chatRoomService.archiveChatRoom(chatRoomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Unarchive a chat room
     */
    @PatchMapping("/{chatRoomId}/unarchive")
    public ResponseEntity<Void> unarchiveChatRoom(@PathVariable String chatRoomId) {
        chatRoomService.unarchiveChatRoom(chatRoomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Pin a chat room
     */
    @PatchMapping("/{chatRoomId}/pin")
    public ResponseEntity<Void> pinChatRoom(@PathVariable String chatRoomId) {
        chatRoomService.pinChatRoom(chatRoomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Unpin a chat room
     */
    @PatchMapping("/{chatRoomId}/unpin")
    public ResponseEntity<Void> unpinChatRoom(@PathVariable String chatRoomId) {
        chatRoomService.unpinChatRoom(chatRoomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search users for chat (returns users with profile photos and handles)
     */
    @GetMapping("/search-users")
    public ResponseEntity<List<UserSummaryDto>> searchUsersForChat(
            @RequestParam String query) {
        String currentUserId = getCurrentUserId();
        List<UserSummaryDto> users = chatRoomService.searchUsersForChat(currentUserId, query);
        return ResponseEntity.ok(users);
    }

    /**
     * Get total unread message count for current user
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getTotalUnreadCount() {
        String currentUserId = getCurrentUserId();
        long count = chatRoomService.getTotalUnreadCount(currentUserId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get unread count for a specific chat room
     */
    @GetMapping("/{chatRoomId}/unread-count")
    public ResponseEntity<Long> getUnreadCountForChatRoom(@PathVariable String chatRoomId) {
        String currentUserId = getCurrentUserId();
        long count = chatRoomService.getUnreadCountForChatRoom(chatRoomId, currentUserId);
        return ResponseEntity.ok(count);
    }

    /**
     * Delete a chat room
     */
    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable String chatRoomId) {
        chatRoomService.deleteChatRoom(chatRoomId);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String principalString && !"anonymousUser".equals(principalString)) {
            return principalString;
        }
        return null;
    }
}
