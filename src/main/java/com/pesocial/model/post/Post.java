package com.pesocial.model.post;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

    private static final String DEFAULT_VISIBILITY = "PUBLIC";
    private static final int MAX_CONTENT_LENGTH = 5000;
    private static final Set<String> SUPPORTED_VISIBILITIES = Set.of("PUBLIC", "FOLLOWERS", "PRIVATE", "EXCLUSIVE");

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
    private String visibility = DEFAULT_VISIBILITY;

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

    private Post(Builder builder) {
        this.authorId = builder.authorId;
        this.contentText = builder.contentText;
        this.media = builder.media;
        this.mediaType = builder.mediaType;
        this.visibility = builder.visibility;
        this.likesCount = 0;
        this.likedBy = new HashSet<>();
        this.sharesCount = 0;
        this.comments = new ArrayList<>();
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder(String authorId) {
        return new Builder(authorId);
    }

    public void editPost(String contentText, MediaUrl media, String mediaType, String visibility) {
        String normalizedContent = normalizeOptional(contentText);
        String normalizedMediaType = normalizeOptional(mediaType);
        if (normalizedMediaType == null && media != null) {
            normalizedMediaType = normalizeOptional(media.getContentType());
        }
        String normalizedVisibility = normalizeVisibility(visibility);

        validatePostPayload(normalizedContent, media, normalizedMediaType);

        this.contentText = normalizedContent;
        this.media = media;
        this.mediaType = normalizedMediaType;
        this.visibility = normalizedVisibility;
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

    public static final class Builder {
        private final String authorId;
        private String contentText;
        private MediaUrl media;
        private String mediaType;
        private String visibility = DEFAULT_VISIBILITY;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        private Builder(String authorId) {
            this.authorId = normalizeRequired("authorId", authorId);
        }

        public Builder contentText(String contentText) {
            this.contentText = normalizeOptional(contentText);
            return this;
        }

        public Builder media(MediaUrl media) {
            this.media = media;
            return this;
        }

        public Builder mediaType(String mediaType) {
            this.mediaType = normalizeOptional(mediaType);
            return this;
        }

        public Builder visibility(String visibility) {
            this.visibility = normalizeVisibility(visibility);
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
            return this;
        }

        public Post build() {
            if (mediaType == null && media != null) {
                mediaType = normalizeOptional(media.getContentType());
            }
            validatePostPayload(contentText, media, mediaType);
            if (updatedAt.isBefore(createdAt)) {
                throw new IllegalArgumentException("updatedAt cannot be before createdAt");
            }
            return new Post(this);
        }
    }

    private static void validatePostPayload(String contentText, MediaUrl media, String mediaType) {
        if (contentText == null && media == null) {
            throw new IllegalArgumentException("A post requires text content or media");
        }
        if (contentText != null && contentText.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("contentText exceeds " + MAX_CONTENT_LENGTH + " characters");
        }
        if (media == null && mediaType != null) {
            throw new IllegalArgumentException("mediaType is not allowed when media is absent");
        }
    }

    private static String normalizeVisibility(String visibility) {
        String normalized = normalizeOptional(visibility);
        if (normalized == null) {
            return DEFAULT_VISIBILITY;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        if ("FRIENDS".equals(upper)) {
            upper = "FOLLOWERS";
        }
        if (!SUPPORTED_VISIBILITIES.contains(upper)) {
            throw new IllegalArgumentException("Unsupported visibility: " + visibility);
        }
        return upper;
    }

    private static String normalizeRequired(String fieldName, String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
