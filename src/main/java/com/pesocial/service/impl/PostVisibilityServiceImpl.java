package com.pesocial.service.impl;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.pesocial.exception.AccessDeniedException;
import com.pesocial.exception.InsufficientFundsException;
import com.pesocial.model.post.Post;
import com.pesocial.model.user.User;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.PostVisibilityService;
import com.pesocial.service.SubscriptionService;

@Service
public class PostVisibilityServiceImpl implements PostVisibilityService {

    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    public PostVisibilityServiceImpl(UserRepository userRepository,
                                     SubscriptionService subscriptionService) {
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public boolean canAccessPost(String viewerId, Post post) {
        if (post == null) {
            return false;
        }

        String visibility = post.getVisibility() == null
            ? "PUBLIC"
            : post.getVisibility().toUpperCase(Locale.ROOT);

        String authorId = post.getAuthorId();

        if ("PUBLIC".equals(visibility)) {
            return true;
        }

        if (viewerId != null && viewerId.equals(authorId)) {
            return true;
        }

        return switch (visibility) {
            case "PRIVATE" -> false;
            case "FOLLOWERS" -> isFollower(viewerId, authorId);
            case "EXCLUSIVE" -> subscriptionService.hasActiveAccessToPost(viewerId, authorId, post.getId());
            default -> false;
        };
    }

    @Override
    public void assertCanAccessPost(String viewerId, Post post) {
        if (canAccessPost(viewerId, post)) {
            return;
        }

        String visibility = post.getVisibility() == null
            ? "PUBLIC"
            : post.getVisibility().toUpperCase(Locale.ROOT);

        if ("EXCLUSIVE".equals(visibility)) {
            throw new InsufficientFundsException("Exclusive content requires an active purchase or subscription");
        }

        throw new AccessDeniedException("You are not allowed to access this post");
    }

    private boolean isFollower(String viewerId, String authorId) {
        if (viewerId == null || viewerId.isBlank()) {
            return false;
        }
        User author = userRepository.findById(authorId).orElse(null);
        return author != null && author.getFollowers().contains(viewerId);
    }
}
