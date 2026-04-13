package com.pesocial.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("emailNotificationStrategy")
public class EmailNotificationStrategy implements NotificationStrategy {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationStrategy.class);

    @Override
    public void send(String recipientUserId, String message) {
        log.info("Email notification to user {}: {}", recipientUserId, message);
    }
}
