package com.pesocial.service;

import java.util.List;

import com.pesocial.model.user.User;

public interface AdminService {
    List<User> viewAllUsers();
    User suspendUser(String userId);
    User banUser(String userId);
    User approveCreator(String userId);
    void removePost(String postId);
    String sendAnnouncement(String message);
    String generateSystemReport();
    List<String> reviewReports();
}
