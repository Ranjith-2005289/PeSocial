package com.pesocial.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pesocial.model.post.Post;
import com.pesocial.model.user.User;
import com.pesocial.repository.PostRepository;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.GuestService;

@Service
public class GuestServiceImpl implements GuestService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public GuestServiceImpl(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Post> viewPublicPosts() {
        return postRepository.findAll().stream()
            .filter(post -> "PUBLIC".equalsIgnoreCase(post.getVisibility()))
            .toList();
    }

    @Override
    public List<User> viewPublicProfiles() {
        return userRepository.findAll().stream()
            .filter(user -> "ACTIVE".equalsIgnoreCase(user.getAccountStatus()))
            .toList();
    }

    @Override
    public List<Post> searchPublicContent(String query) {
        String normalized = query == null ? "" : query.toLowerCase();
        return viewPublicPosts().stream()
            .filter(post -> post.getContentText() != null && post.getContentText().toLowerCase().contains(normalized))
            .toList();
    }
}
