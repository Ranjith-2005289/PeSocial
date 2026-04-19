package com.pesocial.service;

import com.pesocial.model.analytics.CreatorAnalytics;
import com.pesocial.model.post.Post;

public interface CreatorService {
    Post uploadPost(Post post);
    Post uploadExclusivePost(Post post);
    void startLiveStream();
    boolean enableMonetization(String creatorId);
    CreatorAnalytics viewAnalytics(String creatorId);
    String generateAnalyticsReport(String creatorId);
    void pinPost(String postId);
}
