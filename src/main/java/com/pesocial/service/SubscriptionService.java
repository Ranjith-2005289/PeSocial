package com.pesocial.service;

public interface SubscriptionService {
    boolean hasActiveAccessToCreator(String viewerId, String creatorId);
    boolean hasActiveAccessToPost(String viewerId, String creatorId, String postId);
}
