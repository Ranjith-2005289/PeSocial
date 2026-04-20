package com.pesocial.service.impl;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.pesocial.factory.PostFactory;
import com.pesocial.model.Story;
import com.pesocial.model.analytics.CreatorAnalytics;
import com.pesocial.model.post.Post;
import com.pesocial.model.user.Creator;
import com.pesocial.model.user.User;
import com.pesocial.repository.CreatorAnalyticsRepository;
import com.pesocial.repository.PostRepository;
import com.pesocial.repository.StoryRepository;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.CreatorService;

@Service
public class CreatorServiceImpl implements CreatorService {

    private final PostRepository postRepository;
    private final PostFactory postFactory;
    private final UserRepository userRepository;
    private final CreatorAnalyticsRepository creatorAnalyticsRepository;
    private final StoryRepository storyRepository;

    public CreatorServiceImpl(PostRepository postRepository,
                              PostFactory postFactory,
                              UserRepository userRepository,
                              CreatorAnalyticsRepository creatorAnalyticsRepository,
                              StoryRepository storyRepository) {
        this.postRepository = postRepository;
        this.postFactory = postFactory;
        this.userRepository = userRepository;
        this.creatorAnalyticsRepository = creatorAnalyticsRepository;
        this.storyRepository = storyRepository;
    }

    @Override
    public Post uploadPost(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("Post payload is required");
        }

        Post normalized = postFactory.createFeedPost(
            post.getAuthorId(),
            post.getContentText(),
            post.getMedia(),
            post.getMediaType(),
            post.getVisibility()
        );

        return postRepository.save(normalized);
    }

    @Override
    public Post uploadExclusivePost(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("Post payload is required");
        }

        Post exclusive = postFactory.createExclusiveCreatorPost(
            post.getAuthorId(),
            post.getContentText(),
            post.getMedia(),
            post.getMediaType()
        );

        return postRepository.save(exclusive);
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
        User user = userRepository.findById(creatorId)
            .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        String handle = user.getHandle() == null ? "" : user.getHandle().trim();
        List<Post> posts = mergePosts(
            postRepository.findByAuthorIdOrderByCreatedAtDesc(creatorId),
            handle.isBlank() ? List.of() : postRepository.findByAuthorIdOrderByCreatedAtDesc(handle)
        );
        List<Story> stories = mergeStories(
            storyRepository.findByAuthorIdOrderByTimestampDesc(creatorId),
            handle.isBlank() ? List.of() : storyRepository.findByAuthorIdOrderByTimestampDesc(handle)
        );

        long totalLikes = posts.stream().mapToLong(Post::getLikesCount).sum();
        long totalShares = posts.stream().mapToLong(Post::getSharesCount).sum();
        long totalViews = stories.stream().mapToLong(story -> story.getViewers() == null ? 0 : story.getViewers().size()).sum();
        long followersCount = user.getFollowers() == null ? 0 : user.getFollowers().size();
        long followingCount = user.getFollowing() == null ? 0 : user.getFollowing().size();

        CreatorAnalytics analytics = creatorAnalyticsRepository.findByCreatorId(creatorId)
            .orElseGet(() -> {
                CreatorAnalytics created = new CreatorAnalytics();
                created.setCreatorId(creatorId);
                return created;
            });

        analytics.updateMetrics(totalViews, totalLikes, totalShares, followersCount, followingCount);
        CreatorAnalytics savedAnalytics = creatorAnalyticsRepository.save(analytics);

        if (user instanceof Creator creator) {
            creator.setCreatorId(creator.getId());
            creator.setFollowersCount((int) followersCount);
            creator.setFollowingCount((int) followingCount);
            creator.setAnalytics(savedAnalytics);
            userRepository.save(creator);
        }

        return savedAnalytics;
    }

    @Override
    public String generateAnalyticsReport(String creatorId) {
        CreatorAnalytics analytics = viewAnalytics(creatorId);
        return analytics.generateAnalyticsReport();
    }

    private List<Post> mergePosts(List<Post> byId, List<Post> byHandle) {
        Map<String, Post> merged = new LinkedHashMap<>();
        byId.forEach(post -> merged.put(post.getId(), post));
        byHandle.forEach(post -> merged.putIfAbsent(post.getId(), post));
        return List.copyOf(merged.values());
    }

    private List<Story> mergeStories(List<Story> byId, List<Story> byHandle) {
        Map<String, Story> merged = new LinkedHashMap<>();
        byId.forEach(story -> merged.put(story.getId(), story));
        byHandle.forEach(story -> merged.putIfAbsent(story.getId(), story));
        return List.copyOf(merged.values());
    }

    @Override
    public void pinPost(String postId) {
        // Hook for pinning system; post could include pin metadata in later iteration.
    }
}
