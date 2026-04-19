package com.pesocial.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.pesocial.dto.message.SendMessageRequest;
import com.pesocial.exception.EntityNotFoundException;
import com.pesocial.model.message.ChatRoom;
import com.pesocial.model.message.Message;
import com.pesocial.repository.ChatRoomRepository;
import com.pesocial.repository.MessageRepository;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.MessageService;
import com.pesocial.service.NotificationService;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public MessageServiceImpl(MessageRepository messageRepository, ChatRoomRepository chatRoomRepository,
                              SimpMessagingTemplate messagingTemplate,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Message sendMessage(SendMessageRequest request) {
        ChatRoom chatRoom = resolveChatRoom(request);

        Message message = new Message();
        message.setChatRoomId(chatRoom.getChatRoomId());
        message.setSenderId(request.senderId());
        message.setReceiverId(request.receiverId());
        message.setMessageText(request.messageText());
        message.setAttachmentUrl(request.attachmentUrl());
        message.setAttachmentType(request.attachmentType());

        Message saved = messageRepository.save(message);

        chatRoom.updateLastMessage(saved.getMessageId(), saved.getMessageText());
        chatRoom.incrementUnreadCount(request.receiverId());
        chatRoomRepository.save(chatRoom);

        if (request.receiverId() != null && !request.receiverId().equals(request.senderId())) {
            userRepository.findById(request.senderId()).ifPresent(sender -> {
                String senderHandle = (sender.getHandle() != null && !sender.getHandle().isBlank())
                    ? sender.getHandle()
                    : sender.getUsername();
                notificationService.sendMessageNotification(request.receiverId(), senderHandle);
            });
        }

        messagingTemplate.convertAndSendToUser(request.receiverId(), "/queue/messages", saved);
        messagingTemplate.convertAndSendToUser(request.senderId(), "/queue/messages", saved);

        return saved;
    }

    private ChatRoom resolveChatRoom(SendMessageRequest request) {
        if (request.chatRoomId() != null && !request.chatRoomId().isBlank()) {
            return chatRoomRepository.findById(request.chatRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Chat room not found"));
        }

        return chatRoomRepository.findBetweenUsers(request.senderId(), request.receiverId())
            .orElseGet(() -> chatRoomRepository.save(new ChatRoom(request.senderId(), request.receiverId())));
    }

    @Override
    public Message getMessageById(String messageId) {
        return messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("Message not found"));
    }

    @Override
    public void deleteMessage(String messageId) {
        messageRepository.deleteById(messageId);
    }

    @Override
    public Message markAsRead(String messageId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        message.markAsRead();
        return messageRepository.save(message);
    }

    @Override
    public List<Message> getInbox(String receiverId) {
        return messageRepository.findByReceiverIdOrderByTimestampDesc(receiverId);
    }
}
