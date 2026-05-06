package com.pesocial.service.notification;
//ocp
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("inAppNotificationStrategy")
public class InAppNotificationStrategy implements NotificationStrategy {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationStrategy.class);

    @Override
    public void send(String recipientUserId, String message) {
        log.info("In-app notification to user {}: {}", recipientUserId, message);
    }
}
