package com.pesocial.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pesocial.model.user.User;
import com.pesocial.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> users() {
        return ResponseEntity.ok(adminService.viewAllUsers());
    }

    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<User> suspend(@PathVariable String userId) {
        return ResponseEntity.ok(adminService.suspendUser(userId));
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<User> ban(@PathVariable String userId) {
        return ResponseEntity.ok(adminService.banUser(userId));
    }

    @PostMapping("/users/{userId}/approve-creator")
    public ResponseEntity<User> approveCreator(@PathVariable String userId) {
        return ResponseEntity.ok(adminService.approveCreator(userId));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> removePost(@PathVariable String postId) {
        adminService.removePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/announcements")
    public ResponseEntity<String> announcement(@RequestParam String message) {
        return ResponseEntity.ok(adminService.sendAnnouncement(message));
    }

    @GetMapping("/reports/system")
    public ResponseEntity<String> report() {
        return ResponseEntity.ok(adminService.generateSystemReport());
    }

    @GetMapping("/reports/review")
    public ResponseEntity<List<String>> reviewReports() {
        return ResponseEntity.ok(adminService.reviewReports());
    }
}
