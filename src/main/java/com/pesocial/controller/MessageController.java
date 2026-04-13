package com.pesocial.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pesocial.dto.message.SendMessageRequest;
import com.pesocial.model.message.Message;
import com.pesocial.security.TargetEntity;
import com.pesocial.service.OwnershipService;
import com.pesocial.service.MessageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final OwnershipService ownershipService;
    private final GridFsTemplate gridFsTemplate;

    public MessageController(MessageService messageService,
                             OwnershipService ownershipService,
                             GridFsTemplate gridFsTemplate) {
        this.messageService = messageService;
        this.ownershipService = ownershipService;
        this.gridFsTemplate = gridFsTemplate;
    }

    @PostMapping
    public ResponseEntity<Message> send(@Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(request));
    }

    @PostMapping("/attachments")
    public ResponseEntity<Map<String, String>> uploadAttachment(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Attachment file is required");
        }

        Document metadata = new Document();
        metadata.put("contentType", file.getContentType());
        metadata.put("kind", "chat-attachment");

        Object fileId = gridFsTemplate.store(
            file.getInputStream(),
            file.getOriginalFilename(),
            file.getContentType(),
            metadata
        );

        return ResponseEntity.ok(Map.of("attachmentUrl", "/api/media/" + fileId));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(@PathVariable String messageId) {
        Message message = messageService.getMessageById(messageId);
        ownershipService.assertOwnership(TargetEntity.MESSAGE, message.getSenderId(), true);
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{messageId}/read")
    public ResponseEntity<Message> markRead(@PathVariable String messageId) {
        return ResponseEntity.ok(messageService.markAsRead(messageId));
    }

    @GetMapping
    public ResponseEntity<List<Message>> inbox(@RequestParam String receiverId) {
        return ResponseEntity.ok(messageService.getInbox(receiverId));
    }
}
