package com.pesocial.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pesocial.model.analytics.CreatorAnalytics;
import com.pesocial.model.system.SystemService;
import com.pesocial.model.user.Creator;
import com.pesocial.model.user.User;
import com.pesocial.model.user.UserRole;
import com.pesocial.repository.CreatorAnalyticsRepository;
import com.pesocial.repository.PostRepository;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CreatorAnalyticsRepository creatorAnalyticsRepository;
    private final SystemService systemService;

    public AdminServiceImpl(UserRepository userRepository,
                            PostRepository postRepository,
                            CreatorAnalyticsRepository creatorAnalyticsRepository,
                            SystemService systemService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.creatorAnalyticsRepository = creatorAnalyticsRepository;
        this.systemService = systemService;
    }

    @Override
    public List<User> viewAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User suspendUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAccountStatus("SUSPENDED");
        return userRepository.save(user);
    }

    @Override
    public User banUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAccountStatus("BANNED");
        return userRepository.save(user);
    }

    @Override
    public User approveCreator(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Creator creator = toCreator(user);
        creator.setRole(UserRole.CREATOR);
        creator.setCreatorId(creator.getId());
        creator.setVerificationStatus(true);

        CreatorAnalytics analytics = creatorAnalyticsRepository.findByCreatorId(creator.getId())
            .orElseGet(() -> {
                CreatorAnalytics created = new CreatorAnalytics();
                created.setCreatorId(creator.getId());
                return creatorAnalyticsRepository.save(created);
            });

        creator.setAnalytics(analytics);
        creator.setFollowersCount(creator.getFollowers() == null ? 0 : creator.getFollowers().size());
        creator.setFollowingCount(creator.getFollowing() == null ? 0 : creator.getFollowing().size());

        systemService.logActivity();
        return userRepository.save(creator);
    }

    @Override
    public void removePost(String postId) {
        postRepository.deleteById(postId);
    }

    @Override
    public String sendAnnouncement(String message) {
        systemService.logActivity();
        return "Announcement sent: " + message;
    }

    @Override
    public String generateSystemReport() {
        systemService.logActivity();
        return "Users=" + userRepository.count() + ", Posts=" + postRepository.count();
    }

    @Override
    public List<String> reviewReports() {
        long suspendedUsers = userRepository.findAll().stream()
            .filter(user -> "SUSPENDED".equalsIgnoreCase(user.getAccountStatus()))
            .count();
        long bannedUsers = userRepository.findAll().stream()
            .filter(user -> "BANNED".equalsIgnoreCase(user.getAccountStatus()))
            .count();
        long unverifiedCreators = userRepository.findAll().stream()
            .filter(user -> user.getRole() == UserRole.CREATOR)
            .filter(user -> !(user instanceof Creator creator) || !creator.isVerificationStatus())
            .count();

        systemService.detectSpam();
        systemService.logActivity();

        return List.of(
            "System moderation review",
            "Suspended users=" + suspendedUsers,
            "Banned users=" + bannedUsers,
            "Creator approvals pending=" + unverifiedCreators,
            "Total users=" + userRepository.count(),
            "Total posts=" + postRepository.count()
        );
    }

    private Creator toCreator(User user) {
        if (user instanceof Creator creator) {
            return creator;
        }

        Creator creator = new Creator();
        creator.setId(user.getId());
        creator.setUsername(user.getUsername());
        creator.setHandle(user.getHandle());
        creator.setEmail(user.getEmail());
        creator.setPasswordHash(user.getPasswordHash());
        creator.setProfilePhoto(user.getProfilePhoto());
        creator.setBio(user.getBio());
        creator.setAccountStatus(user.getAccountStatus());
        creator.setRole(UserRole.CREATOR);
        creator.setFollowers(user.getFollowers());
        creator.setFollowing(user.getFollowing());
        creator.setCreatedAt(user.getCreatedAt());
        creator.setUpdatedAt(user.getUpdatedAt());
        creator.setLastNotificationType(user.getLastNotificationType());
        creator.setLastNotificationAt(user.getLastNotificationAt());
        return creator;
    }
}
