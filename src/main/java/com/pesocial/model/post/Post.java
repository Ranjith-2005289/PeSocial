package com.pesocial.model.post;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    public void addLike() {
        likesCount++;
        updatedAt = Instant.now();
    }

    public void removeLike() {
        if (likesCount > 0) {
            likesCount--;
        }
        updatedAt = Instant.now();
    }

    public void addComment(String comment) {
        comments.add(comment);
        updatedAt = Instant.now();
    }

    public void sharePost() {
        sharesCount++;
        updatedAt = Instant.now();
    }
}
