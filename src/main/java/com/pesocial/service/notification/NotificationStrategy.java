package com.pesocial.service.notification;

public interface NotificationStrategy {
    void send(String recipientUserId, String message);
}
