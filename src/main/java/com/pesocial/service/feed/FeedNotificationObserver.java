package com.pesocial.service.feed;

import org.springframework.stereotype.Component;

import com.pesocial.dto.notification.CreateNotificationRequest;
import com.pesocial.model.notification.NotificationType;
import com.pesocial.model.user.User;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.NotificationService;
import com.pesocial.service.notification.NotificationContext;

import jakarta.annotation.PostConstruct;

@Component
public class FeedNotificationObserver implements FollowerObserver {

    private final FeedPublisher feedPublisher;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final NotificationContext notificationContext;

    public FeedNotificationObserver(FeedPublisher feedPublisher,
                                    UserRepository userRepository,
                                    NotificationService notificationService,
                                    NotificationContext notificationContext) {
        this.feedPublisher = feedPublisher;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.notificationContext = notificationContext;
    }

    @PostConstruct
    public void register() {
        feedPublisher.subscribe(this);
    }

    @Override
    public void onPostPublished(String authorId, String postId) {
        User author = userRepository.findById(authorId).orElse(null);
        if (author == null) {
            return;
        }

        String senderHandle = author.getHandle() != null && !author.getHandle().isBlank()
            ? author.getHandle()
            : author.getUsername();

        for (String followerId : author.getFollowers()) {
            String msg = String.format("%s created a new post", author.getUsername());
            notificationService.sendNotification(new CreateNotificationRequest(followerId, senderHandle, NotificationType.MESSAGE));
            notificationContext.notifyInApp(followerId, msg);
        }
    }
}
