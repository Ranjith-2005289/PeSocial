package com.pesocial.service;

import java.util.List;
import java.util.Optional;

import com.pesocial.dto.auth.AuthResponse;
import com.pesocial.dto.user.UserProfileDto;
import com.pesocial.dto.user.UserSummaryDto;
import com.pesocial.model.post.Post;
import com.pesocial.model.user.User;

public interface UserService {
    Optional<User> findById(String userId);
    Optional<User> findByHandle(String handle);
    void follow(String followerId, String followeeId);
    void followByHandle(String followerId, String followeeHandle);
    void unfollow(String followerId, String followeeId);
    void unfollowByHandle(String followerId, String followeeHandle);
    void removeFollowerByHandle(String userId, String followerHandle);
    List<User> searchUser(String handle);
    List<UserProfileDto> searchUserProfiles(String handle);
    UserProfileDto getUserProfile(String userId);
    UserProfileDto getUserProfileByHandle(String handle);
    List<UserSummaryDto> getFollowersByHandle(String handle, int page, int size);
    List<UserSummaryDto> getFollowingByHandle(String handle, int page, int size);
    List<Post> viewFeed(String userId);
    User updateProfile(String userId, String username, String profilePhoto, String bio);
    User updateMyProfile(String userId, String username, String handle, String bio);
    User updateProfilePhoto(String userId, String profilePhoto);
    AuthResponse becomeCreator(String userId);
    List<String> getFollowingIds(String userId);
}
