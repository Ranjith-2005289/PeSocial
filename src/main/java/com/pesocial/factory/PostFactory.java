package com.pesocial.factory;

import org.springframework.stereotype.Component;

import com.pesocial.model.post.MediaUrl;
import com.pesocial.model.post.Post;

@Component
public class PostFactory {

    public Post createFeedPost(String authorId, String contentText, MediaUrl media, String mediaType, String visibility) {
        return Post.builder(authorId)
            .contentText(contentText)
            .media(media)
            .mediaType(mediaType)
            .visibility(visibility)
            .build();
    }

    public Post createExclusiveCreatorPost(String authorId, String contentText, MediaUrl media, String mediaType) {
        return Post.builder(authorId)
            .contentText(contentText)
            .media(media)
            .mediaType(mediaType)
            .visibility("EXCLUSIVE")
            .build();
    }
}
