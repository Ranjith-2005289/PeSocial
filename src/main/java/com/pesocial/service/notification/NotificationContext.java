package com.pesocial.service.notification;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
public class NotificationContext {

    private final NotificationStrategy inAppNotificationStrategy;
    private final NotificationStrategy emailNotificationStrategy;

    public NotificationContext(@Qualifier("inAppNotificationStrategy") NotificationStrategy inAppNotificationStrategy,
                               @Qualifier("emailNotificationStrategy") NotificationStrategy emailNotificationStrategy) {
        this.inAppNotificationStrategy = inAppNotificationStrategy;
        this.emailNotificationStrategy = emailNotificationStrategy;
    }

    public void notifyInApp(String recipientUserId, String message) {
        inAppNotificationStrategy.send(recipientUserId, message);
    }

    public void notifyEmail(String recipientUserId, String message) {
        emailNotificationStrategy.send(recipientUserId, message);
    }
}
