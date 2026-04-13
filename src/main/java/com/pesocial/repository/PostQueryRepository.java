package com.pesocial.repository;

import java.util.List;

import com.pesocial.model.post.Post;

public interface PostQueryRepository {
    List<Post> findVisibleByAuthorForViewer(String authorId, String viewerId);
    List<Post> findVisibleForViewer(String viewerId);
}
