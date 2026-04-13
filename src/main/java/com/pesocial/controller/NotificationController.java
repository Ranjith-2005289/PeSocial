package com.pesocial.controller;

import java.util.List;

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

import com.pesocial.dto.notification.CreateNotificationRequest;
import com.pesocial.model.notification.Notification;
import com.pesocial.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Notification> send(@Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markRead(@PathVariable String notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead(@RequestParam String recipientId) {
        notificationService.markAllAsRead(recipientId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> delete(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Notification>> list(@RequestParam String recipientId) {
        return ResponseEntity.ok(notificationService.getNotifications(recipientId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> countUnread(@RequestParam String recipientId) {
        return ResponseEntity.ok(notificationService.countUnread(recipientId));
    }
}
