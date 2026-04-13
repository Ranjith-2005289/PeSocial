package com.pesocial.service.impl;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pesocial.dto.message.ChatRoomDTO;
import com.pesocial.dto.message.MessageDTO;
import com.pesocial.dto.user.UserSummaryDto;
import com.pesocial.exception.EntityNotFoundException;
import com.pesocial.model.message.ChatRoom;
import com.pesocial.model.message.Message;
import com.pesocial.model.user.User;
import com.pesocial.repository.ChatRoomRepository;
import com.pesocial.repository.MessageRepository;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.ChatRoomService;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatRoomServiceImpl(ChatRoomRepository chatRoomRepository,
                             MessageRepository messageRepository,
                             UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ChatRoom createOrGetChatRoom(String userId1, String userId2) {
        // Check if chat room already exists
        var existingChatRoom = chatRoomRepository.findBetweenUsers(userId1, userId2);
        if (existingChatRoom.isPresent()) {
            return existingChatRoom.get();
        }

        // Create new chat room
        ChatRoom chatRoom = new ChatRoom(userId1, userId2);
        return chatRoomRepository.save(chatRoom);
    }

    @Override
    public ChatRoomDTO createOrGetChatRoomByHandle(String currentUserId, String targetHandle) {
        String normalizedHandle = normalizeHandle(targetHandle);
        User targetUser = userRepository.findByHandle(normalizedHandle)
            .orElseThrow(() -> new EntityNotFoundException("Target user not found"));

        ChatRoom chatRoom = createOrGetChatRoom(currentUserId, targetUser.getId());
        Long unreadCount = chatRoom.getUnreadCount(currentUserId);

        return new ChatRoomDTO(
            chatRoom.getChatRoomId(),
            targetUser.getId(),
            targetUser.getHandle(),
            targetUser.getUsername(),
            targetUser.getProfilePhoto(),
            chatRoom.getLastMessageText(),
            chatRoom.getLastMessageTimestamp(),
            unreadCount,
            chatRoom.getPinned(),
            chatRoom.getArchived()
        );
    }

    @Override
    public ChatRoom getChatRoom(String chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new EntityNotFoundException("Chat room not found"));
    }

    @Override
    public List<ChatRoom> getAllChatRoomsForUser(String userId) {
        return chatRoomRepository.findAllForUserOrderByRecent(userId);
    }

    @Override
    public List<ChatRoomDTO> getAllChatRoomsWithDetails(String userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllForUserOrderByRecent(userId);
        
        return chatRooms.stream()
            .map(chatRoom -> {
                String otherUserId = chatRoom.getOtherUserId(userId);
                User otherUser = userRepository.findById(otherUserId).orElse(null);
                
                Long unreadCount = chatRoom.getUnreadCount(userId);
                
                return new ChatRoomDTO(
                    chatRoom.getChatRoomId(),
                    otherUserId,
                    otherUser != null ? otherUser.getHandle() : "",
                    otherUser != null ? otherUser.getUsername() : "",
                    otherUser != null ? otherUser.getProfilePhoto() : "",
                    chatRoom.getLastMessageText(),
                    chatRoom.getLastMessageTimestamp(),
                    unreadCount,
                    chatRoom.getPinned(),
                    chatRoom.getArchived()
                );
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<Message> getChatHistory(String chatRoomId, int limit, int offset) {
        List<Message> allMessages = messageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
        
        int start = Math.min(offset, allMessages.size());
        int end = Math.min(offset + limit, allMessages.size());
        
        return allMessages.subList(start, end);
    }

    @Override
    public List<MessageDTO> getChatHistoryDTO(String chatRoomId, int limit, int offset) {
        List<Message> messages = getChatHistory(chatRoomId, limit, offset);
        
        return messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void markChatRoomAsRead(String chatRoomId, String userId) {
        // Mark all unread messages in this chat room as read
        List<Message> unreadMessages = messageRepository.findUnreadInChatRoom(chatRoomId, userId);
        
        for (Message message : unreadMessages) {
            message.markAsRead();
            messageRepository.save(message);
        }

        // Update chat room unread count
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        chatRoom.resetUnreadCount(userId);
        chatRoomRepository.save(chatRoom);
    }

    @Override
    public void archiveChatRoom(String chatRoomId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        chatRoom.setArchived(true);
        chatRoomRepository.save(chatRoom);
    }

    @Override
    public void unarchiveChatRoom(String chatRoomId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        chatRoom.setArchived(false);
        chatRoomRepository.save(chatRoom);
    }

    @Override
    public void pinChatRoom(String chatRoomId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        chatRoom.setPinned(true);
        chatRoomRepository.save(chatRoom);
    }

    @Override
    public void unpinChatRoom(String chatRoomId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        chatRoom.setPinned(false);
        chatRoomRepository.save(chatRoom);
    }

    @Override
    public List<UserSummaryDto> searchUsersForChat(String currentUserId, String searchQuery) {
        String normalizedQuery = normalizeSearchQuery(searchQuery).toLowerCase(Locale.ROOT);

        Map<String, UserSummaryDto> uniqueUsers = new LinkedHashMap<>();

        userRepository.findAll()
            .stream()
            .filter(user -> !user.getId().equals(currentUserId))
            .filter(user -> matchesUserSearch(user, normalizedQuery))
            .forEach(user -> uniqueUsers.putIfAbsent(user.getId(), toUserSummary(user)));

        return uniqueUsers.values().stream().collect(Collectors.toList());
    }

    @Override
    public long getTotalUnreadCount(String userId) {
        return messageRepository.countAllUnread(userId);
    }

    @Override
    public long getUnreadCountForChatRoom(String chatRoomId, String userId) {
        return messageRepository.countUnreadInChatRoom(chatRoomId, userId);
    }

    @Override
    public void deleteChatRoom(String chatRoomId) {
        // Delete all messages in the chat room
        List<Message> messages = messageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
        messageRepository.deleteAll(messages);
        
        // Delete the chat room
        chatRoomRepository.deleteById(chatRoomId);
    }

    /**
     * Convert Message to MessageDTO
     */
    private MessageDTO convertToDTO(Message message) {
        return new MessageDTO(
            message.getMessageId(),
            message.getChatRoomId(),
            message.getSenderId(),
            message.getReceiverId(),
            message.getMessageText(),
            message.getTimestamp(),
            message.getMessageStatus(),
            message.getIsRead(),
            message.getReaction(),
            message.getAttachmentUrl(),
            message.getAttachmentType()
        );
    }

    private UserSummaryDto toUserSummary(User user) {
        return new UserSummaryDto(
            user.getId(),
            user.getHandle(),
            user.getProfilePhoto()
        );
    }

    private String normalizeSearchQuery(String query) {
        if (query == null) {
            return "";
        }
        return query.trim().replaceFirst("^@", "");
    }

    private boolean matchesUserSearch(User user, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return true;
        }

        String username = user.getUsername() != null ? user.getUsername().toLowerCase(Locale.ROOT) : "";
        String handle = user.getHandle() != null ? user.getHandle().toLowerCase(Locale.ROOT) : "";
        String email = user.getEmail() != null ? user.getEmail().toLowerCase(Locale.ROOT) : "";

        String plainQuery = normalizedQuery.replaceFirst("^@", "");
        return username.contains(normalizedQuery)
            || handle.contains(normalizedQuery)
            || handle.contains("@" + plainQuery)
            || email.contains(normalizedQuery);
    }

    private String normalizeHandle(String handle) {
        if (handle == null) {
            return null;
        }
        return handle.startsWith("@") ? handle : "@" + handle;
    }
}
