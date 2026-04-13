package com.pesocial.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.pesocial.dto.auth.AuthResponse;
import com.pesocial.dto.user.UserProfileDto;
import com.pesocial.dto.user.UserSummaryDto;
import com.pesocial.exception.EntityNotFoundException;
import com.pesocial.model.analytics.CreatorAnalytics;
import com.pesocial.model.post.Post;
import com.pesocial.model.user.User;
import com.pesocial.model.user.UserRole;
import com.pesocial.repository.CreatorAnalyticsRepository;
import com.pesocial.repository.PostRepository;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.NotificationService;
import com.pesocial.service.UserService;
import com.pesocial.service.security.JwtService;
import com.pesocial.service.security.RefreshTokenService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CreatorAnalyticsRepository creatorAnalyticsRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final NotificationService notificationService;

    public UserServiceImpl(UserRepository userRepository,
                           PostRepository postRepository,
                           CreatorAnalyticsRepository creatorAnalyticsRepository,
                           JwtService jwtService,
                           RefreshTokenService refreshTokenService,
                           NotificationService notificationService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.creatorAnalyticsRepository = creatorAnalyticsRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.notificationService = notificationService;
    }

    @Override
    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> findByHandle(String handle) {
        return userRepository.findByHandle(normalizeHandle(handle));
    }

    @Override
    public void follow(String followerId, String followeeId) {
        User follower = userRepository.findById(followerId)
            .orElseThrow(() -> new EntityNotFoundException("Follower user not found"));
        User followee = userRepository.findById(followeeId)
            .orElseThrow(() -> new EntityNotFoundException("Followee user not found"));

        follower.follow(followeeId);
        followee.addFollower(followerId);

        userRepository.save(follower);
        userRepository.save(followee);

        String senderHandle = follower.getHandle() != null && !follower.getHandle().isBlank()
            ? follower.getHandle()
            : follower.getUsername();
        notificationService.sendFollowNotification(followee.getId(), senderHandle);
    }

    @Override
    public void followByHandle(String followerId, String followeeHandle) {
        User followee = userRepository.findByHandle(normalizeHandle(followeeHandle))
            .orElseThrow(() -> new EntityNotFoundException("Followee user not found"));
        follow(followerId, followee.getId());
    }

    @Override
    public void unfollow(String followerId, String followeeId) {
        User follower = userRepository.findById(followerId)
            .orElseThrow(() -> new EntityNotFoundException("Follower user not found"));
        User followee = userRepository.findById(followeeId)
            .orElseThrow(() -> new EntityNotFoundException("Followee user not found"));

        follower.unfollow(followeeId);
        followee.removeFollower(followerId);

        userRepository.save(follower);
        userRepository.save(followee);
    }

    @Override
    public void unfollowByHandle(String followerId, String followeeHandle) {
        User followee = userRepository.findByHandle(normalizeHandle(followeeHandle))
            .orElseThrow(() -> new EntityNotFoundException("Followee user not found"));
        unfollow(followerId, followee.getId());
    }

    @Override
    public void removeFollowerByHandle(String userId, String followerHandle) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        User follower = userRepository.findByHandle(normalizeHandle(followerHandle))
            .orElseThrow(() -> new EntityNotFoundException("Follower not found"));

        user.removeFollower(follower.getId());
        follower.unfollow(user.getId());

        userRepository.save(user);
        userRepository.save(follower);
    }

    @Override
    public List<User> searchUser(String handle) {
        String normalized = normalizeSearchTerm(handle).toLowerCase(Locale.ROOT);
        Map<String, User> uniqueUsers = new LinkedHashMap<>();

        userRepository.findAll()
            .stream()
            .filter(user -> matchesUserSearch(user, normalized))
            .forEach(user -> uniqueUsers.putIfAbsent(user.getId(), user));

        return new ArrayList<>(uniqueUsers.values());
    }

    @Override
    public List<UserProfileDto> searchUserProfiles(String handle) {
        return searchUser(handle)
            .stream()
            .map(this::toUserProfile)
            .toList();
    }

    @Override
    public UserProfileDto getUserProfile(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toUserProfile(user);
    }

    @Override
    public UserProfileDto getUserProfileByHandle(String handle) {
        User user = userRepository.findByHandle(normalizeHandle(handle))
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toUserProfile(user);
    }

    @Override
    public List<UserSummaryDto> getFollowersByHandle(String handle, int page, int size) {
        User user = userRepository.findByHandle(normalizeHandle(handle))
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toPaginatedSummaries(user.getFollowers(), page, size);
    }

    @Override
    public List<UserSummaryDto> getFollowingByHandle(String handle, int page, int size) {
        User user = userRepository.findByHandle(normalizeHandle(handle))
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toPaginatedSummaries(user.getFollowing(), page, size);
    }

    @Override
    public List<Post> viewFeed(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Post> feed = new ArrayList<>();
        for (String followedUserId : user.getFollowing()) {
            feed.addAll(postRepository.findByAuthorIdOrderByCreatedAtDesc(followedUserId));
        }
        return feed;
    }

    @Override
    public User updateProfile(String userId, String username, String profilePhoto, String bio) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.updateProfile(username, profilePhoto, bio);
        return userRepository.save(user);
    }

    @Override
    public User updateMyProfile(String userId, String username, String handle, String bio) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String normalizedHandle = requireHandle(handle);
        if (!normalizedHandle.equals(user.getHandle()) && userRepository.existsByHandle(normalizedHandle)) {
            throw new IllegalArgumentException("Handle already taken");
        }

        user.setHandle(normalizedHandle);
        user.updateProfile(username, user.getProfilePhoto(), bio);
        return userRepository.save(user);
    }

    @Override
    public User updateProfilePhoto(String userId, String profilePhoto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setProfilePhoto(profilePhoto);
        return userRepository.save(user);
    }

    @Override
    public AuthResponse becomeCreator(String userId) {
        User existingUser = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User creatorUser = existingUser;
        if (creatorUser.getRole() != UserRole.CREATOR) {
            creatorUser.setRole(UserRole.CREATOR);
            creatorUser = userRepository.save(creatorUser);
        }

        final User finalCreatorUser = creatorUser;
        creatorAnalyticsRepository.findByCreatorId(finalCreatorUser.getId())
            .orElseGet(() -> {
                CreatorAnalytics analytics = new CreatorAnalytics();
                analytics.setCreatorId(finalCreatorUser.getId());
                return creatorAnalyticsRepository.save(analytics);
            });

        refreshTokenService.revokeAllForUser(finalCreatorUser.getId());

        String accessToken = jwtService.generateAccessToken(finalCreatorUser.getId(), finalCreatorUser.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(finalCreatorUser.getId(), finalCreatorUser.getRole().name());
        refreshTokenService.create(finalCreatorUser.getId(), finalCreatorUser.getRole().name(), refreshToken);

        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            900,
            finalCreatorUser.getId(),
            finalCreatorUser.getRole().name(),
            finalCreatorUser.getHandle()
        );
    }

    private UserProfileDto toUserProfile(User user) {
        return new UserProfileDto(
            user.getId(),
            user.getUsername(),
            user.getHandle(),
            user.getEmail(),
            user.getProfilePhoto(),
            user.getBio(),
            user.getRole().name(),
            user.getFollowers().size(),
            user.getFollowing().size()
        );
    }

    private String normalizeHandle(String handle) {
        String trimmed = handle == null ? "" : handle.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isBlank()) {
            return "@";
        }
        return trimmed.startsWith("@") ? trimmed : "@" + trimmed;
    }

    private String requireHandle(String handle) {
        String normalized = normalizeHandle(handle);
        if ("@".equals(normalized)) {
            throw new IllegalArgumentException("Handle is required");
        }
        return normalized;
    }

    private String normalizeSearchTerm(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceFirst("^@", "");
    }

    private boolean matchesUserSearch(User user, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return true;
        }

        String username = user.getUsername() != null ? user.getUsername().toLowerCase(Locale.ROOT) : "";
        String handle = user.getHandle() != null ? user.getHandle().toLowerCase(Locale.ROOT) : "";
        String email = user.getEmail() != null ? user.getEmail().toLowerCase(Locale.ROOT) : "";

        String plainQuery = normalizedQuery.replaceFirst("^@", "");
        return username.contains(normalizedQuery)
            || handle.contains(normalizedQuery)
            || handle.contains("@" + plainQuery)
            || email.contains(normalizedQuery);
    }

    private List<UserSummaryDto> toPaginatedSummaries(Set<String> userIds, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        List<UserSummaryDto> summaries = userRepository.findAllById(userIds)
            .stream()
            .map(this::toUserSummary)
            .sorted((a, b) -> a.handle().compareToIgnoreCase(b.handle()))
            .toList();

        int fromIndex = Math.min(safePage * safeSize, summaries.size());
        int toIndex = Math.min(fromIndex + safeSize, summaries.size());
        return summaries.subList(fromIndex, toIndex);
    }

    private UserSummaryDto toUserSummary(User user) {
        return new UserSummaryDto(user.getId(), user.getHandle(), user.getProfilePhoto());
    }

    @Override
    public List<String> getFollowingIds(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new ArrayList<>();
        }
        User user = userOpt.get();
        return new ArrayList<>(user.getFollowing() != null ? user.getFollowing() : new ArrayList<>());
    }
}

