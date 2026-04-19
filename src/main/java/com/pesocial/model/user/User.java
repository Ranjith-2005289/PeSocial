package com.pesocial.model.user;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.pesocial.model.notification.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
@TypeAlias("user")
public abstract class User {

    @Id
    private String id;

    @Field("username")
    private String username;

    @Indexed(unique = true, sparse = true)
    @Field("handle")
    private String handle;

    @Field("email")
    private String email;

    @Field("password_hash")
    private String passwordHash;

    @Field("profile_photo")
    private String profilePhoto;

    @Field("bio")
    private String bio;

    @Field("account_status")
    private String accountStatus = "ACTIVE";

    @Field("role")
    private UserRole role;

    @Field("followers")
    private Set<String> followers = new HashSet<>();

    @Field("following")
    private Set<String> following = new HashSet<>();

    @Field("created_at")
    private Instant createdAt = Instant.now();

    @Field("updated_at")
    private Instant updatedAt = Instant.now();

    @Field("last_notification_type")
    private String lastNotificationType;

    @Field("last_notification_at")
    private Instant lastNotificationAt;

    public void follow(String userId) {
        following.add(userId);
        updatedAt = Instant.now();
    }

    public void unfollow(String userId) {
        following.remove(userId);
        updatedAt = Instant.now();
    }

    public void addFollower(String userId) {
        followers.add(userId);
        updatedAt = Instant.now();
    }

    public void removeFollower(String userId) {
        followers.remove(userId);
        updatedAt = Instant.now();
    }

    public void updateProfile(String username, String profilePhoto, String bio) {
        this.username = username;
        this.profilePhoto = profilePhoto;
        this.bio = bio;
        this.updatedAt = Instant.now();
    }

    public void changePassword(String newHash) {
        this.passwordHash = newHash;
        this.updatedAt = Instant.now();
    }

    public void deleteAccount() {
        this.accountStatus = "DELETED";
        this.updatedAt = Instant.now();
    }

    public void receiveNotification(NotificationType type) {
        this.lastNotificationType = type == null ? null : type.name();
        this.lastNotificationAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public abstract String getUserType();
}
