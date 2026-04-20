package com.pesocial.model.post;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    @NotBlank
    @Field("author_id")
    private String authorId;

    @Field("content_text")
    private String contentText;

    @Field("media")
    private MediaUrl media;

    @Field("media_type")
    private String mediaType;

    @Field("visibility")
    private String visibility = "PUBLIC";

    @Field("likes_count")
    private int likesCount;

    @Field("liked_by")
    private Set<String> likedBy = new HashSet<>();

    @Field("shares_count")
    private int sharesCount;

    @Field("comments")
    private List<String> comments = new ArrayList<>();

    @Field("created_at")
    private Instant createdAt = Instant.now();

    @Field("updated_at")
    private Instant updatedAt = Instant.now();

    public void editPost(String contentText, MediaUrl media, String mediaType, String visibility) {
        this.contentText = contentText;
        this.media = media;
        this.mediaType = mediaType;
        this.visibility = visibility;
        this.updatedAt = Instant.now();
    }

    public boolean addLike(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        boolean added = likedBy.add(userId);
        if (!added) {
            return false;
        }
        likesCount++;
        updatedAt = Instant.now();
        return true;
    }

    public boolean removeLike(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        boolean removed = likedBy.remove(userId);
        if (!removed) {
            return false;
        }
        likesCount = Math.max(0, likesCount - 1);
        updatedAt = Instant.now();
        return true;
    }

    public void addComment(String userId, String comment) {
        String normalizedUserId = userId == null || userId.isBlank() ? "unknown-user" : userId.trim();
        String normalizedComment = comment == null ? "" : comment.trim();
        comments.add(normalizedUserId + "|||" + normalizedComment);
        updatedAt = Instant.now();
    }

    public void sharePost() {
        sharesCount++;
        updatedAt = Instant.now();
    }
}
