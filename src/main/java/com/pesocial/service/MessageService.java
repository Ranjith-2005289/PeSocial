package com.pesocial.service;

import java.util.List;

import com.pesocial.dto.message.SendMessageRequest;
import com.pesocial.model.message.Message;

public interface MessageService {
    Message getMessageById(String messageId);
    Message sendMessage(SendMessageRequest request);
    void deleteMessage(String messageId);
    Message markAsRead(String messageId);
    List<Message> getInbox(String receiverId);
}
