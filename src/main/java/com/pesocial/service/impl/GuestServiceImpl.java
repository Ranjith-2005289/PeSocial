package com.pesocial.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pesocial.model.post.Post;
import com.pesocial.model.system.SystemService;
import com.pesocial.model.user.User;
import com.pesocial.repository.PostRepository;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.GuestService;

@Service
public class GuestServiceImpl implements GuestService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SystemService systemService;

    public GuestServiceImpl(PostRepository postRepository,
                            UserRepository userRepository,
                            SystemService systemService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.systemService = systemService;
    }

    @Override
    public List<Post> viewPublicPosts() {
        systemService.enforceGuestTimeLimit();
        systemService.logActivity();
        return postRepository.findAll().stream()
            .filter(post -> "PUBLIC".equalsIgnoreCase(post.getVisibility()))
            .toList();
    }

    @Override
    public List<User> viewPublicProfiles() {
        systemService.enforceGuestTimeLimit();
        systemService.logActivity();
        return userRepository.findAll().stream()
            .filter(user -> "ACTIVE".equalsIgnoreCase(user.getAccountStatus()))
            .toList();
    }

    @Override
    public List<Post> searchPublicContent(String query) {
        systemService.enforceGuestTimeLimit();
        systemService.recommendPosts();
        systemService.logActivity();
        String normalized = query == null ? "" : query.toLowerCase();
        return viewPublicPosts().stream()
            .filter(post -> post.getContentText() != null && post.getContentText().toLowerCase().contains(normalized))
            .toList();
    }
}
