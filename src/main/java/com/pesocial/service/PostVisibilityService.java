package com.pesocial.service;

import com.pesocial.model.post.Post;

public interface PostVisibilityService {
    boolean canAccessPost(String viewerId, Post post);
    void assertCanAccessPost(String viewerId, Post post);
}
