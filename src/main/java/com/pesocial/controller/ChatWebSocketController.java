package com.pesocial.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.pesocial.dto.message.SendMessageRequest;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.MessageService;

@Controller
public class ChatWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public ChatWebSocketController(MessageService messageService,
                                   SimpMessagingTemplate messagingTemplate,
                                   UserRepository userRepository) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @MessageMapping("/dm")
    public void sendDirectMessage(SendMessageRequest request) {
        messageService.sendMessage(request);
    }

    @MessageMapping("/typing")
    public void typing(ChatTypingEvent event) {
        messagingTemplate.convertAndSendToUser(event.toUserId(), "/queue/messages", event);
    }

    @MessageMapping("/seen")
    public void seen(ChatSeenEvent event) {
        messagingTemplate.convertAndSendToUser(event.receiverId(), "/queue/messages", event);
    }

    @MessageMapping("/call-signal")
    public void callSignal(CallSignalEvent event) {
        if (event == null || event.toUserId() == null || event.toUserId().isBlank()) {
            return;
        }

        // Primary route by authenticated principal ID
        messagingTemplate.convertAndSendToUser(event.toUserId(), "/queue/calls", event);

        // Secondary route by handle (for sessions authenticated with handle principal)
        userRepository.findById(event.toUserId()).ifPresent(user -> {
            String handle = user.getHandle();
            if (handle != null && !handle.isBlank()) {
                messagingTemplate.convertAndSendToUser(handle, "/queue/calls", event);
            }
        });

        // Fallback private topic route keyed by target user id
        messagingTemplate.convertAndSend("/topic/calls/" + event.toUserId(), event);
    }

    public record ChatTypingEvent(String toUserId, String fromUserId, boolean typing, String chatRoomId) {}
    public record ChatSeenEvent(String chatRoomId, String messageId, String senderId, String receiverId, boolean seen) {}
    public record CallSignalEvent(
        String type,
        String fromUserId,
        String toUserId,
        String callType,
        Object sdp,
        Object candidate,
        String chatRoomId
    ) {}
}
