package com.pesocial.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pesocial.model.user.User;
import com.pesocial.repository.PostRepository;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public AdminServiceImpl(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    public List<User> viewAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User suspendUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAccountStatus("SUSPENDED");
        return userRepository.save(user);
    }

    @Override
    public User banUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAccountStatus("BANNED");
        return userRepository.save(user);
    }

    @Override
    public void removePost(String postId) {
        postRepository.deleteById(postId);
    }

    @Override
    public String sendAnnouncement(String message) {
        return "Announcement sent: " + message;
    }

    @Override
    public String generateSystemReport() {
        return "Users=" + userRepository.count() + ", Posts=" + postRepository.count();
    }
}
