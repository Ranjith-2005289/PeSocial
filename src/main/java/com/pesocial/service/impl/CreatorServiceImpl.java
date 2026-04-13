package com.pesocial.service.impl;

import org.springframework.stereotype.Service;

import com.pesocial.model.analytics.CreatorAnalytics;
import com.pesocial.model.post.Post;
import com.pesocial.model.user.Creator;
import com.pesocial.model.user.User;
import com.pesocial.repository.CreatorAnalyticsRepository;
import com.pesocial.repository.PostRepository;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.CreatorService;

@Service
public class CreatorServiceImpl implements CreatorService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CreatorAnalyticsRepository creatorAnalyticsRepository;

    public CreatorServiceImpl(PostRepository postRepository,
                              UserRepository userRepository,
                              CreatorAnalyticsRepository creatorAnalyticsRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.creatorAnalyticsRepository = creatorAnalyticsRepository;
    }

    @Override
    public Post uploadPost(Post post) {
        return postRepository.save(post);
    }

    @Override
    public Post uploadExclusivePost(Post post) {
        post.setVisibility("EXCLUSIVE");
        return postRepository.save(post);
    }

    @Override
    public void startLiveStream() {
        // Hook for live stream service integration.
    }

    @Override
    public boolean enableMonetization(String creatorId) {
        User user = userRepository.findById(creatorId)
            .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        if (!(user instanceof Creator creator)) {
            throw new IllegalArgumentException("User is not a creator");
        }
        creator.enableMonetization();
        userRepository.save(creator);
        return creator.isMonetizationEnabled();
    }

    @Override
    public CreatorAnalytics viewAnalytics(String creatorId) {
        return creatorAnalyticsRepository.findByCreatorId(creatorId)
            .orElseGet(() -> {
                CreatorAnalytics analytics = new CreatorAnalytics();
                analytics.setCreatorId(creatorId);
                return creatorAnalyticsRepository.save(analytics);
            });
    }

    @Override
    public void pinPost(String postId) {
        // Hook for pinning system; post could include pin metadata in later iteration.
    }
}
