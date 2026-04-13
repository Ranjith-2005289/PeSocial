package com.pesocial.service;

import java.util.List;

import com.pesocial.model.post.Post;
import com.pesocial.model.user.User;

public interface GuestService {
    List<Post> viewPublicPosts();
    List<User> viewPublicProfiles();
    List<Post> searchPublicContent(String query);
}
