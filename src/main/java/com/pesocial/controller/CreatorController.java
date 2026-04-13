package com.pesocial.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pesocial.model.analytics.CreatorAnalytics;
import com.pesocial.model.post.Post;
import com.pesocial.service.CreatorService;

@RestController
@RequestMapping("/api/creators")
public class CreatorController {

    private final CreatorService creatorService;

    public CreatorController(CreatorService creatorService) {
        this.creatorService = creatorService;
    }

    @PostMapping("/posts")
    public ResponseEntity<Post> uploadPost(@RequestBody Post post) {
        return ResponseEntity.ok(creatorService.uploadPost(post));
    }

    @PostMapping("/posts/exclusive")
    public ResponseEntity<Post> uploadExclusivePost(@RequestBody Post post) {
        return ResponseEntity.ok(creatorService.uploadExclusivePost(post));
    }

    @PatchMapping("/{creatorId}/monetization")
    public ResponseEntity<Boolean> enableMonetization(@PathVariable String creatorId) {
        return ResponseEntity.ok(creatorService.enableMonetization(creatorId));
    }

    @GetMapping("/{creatorId}/analytics")
    public ResponseEntity<CreatorAnalytics> analytics(@PathVariable String creatorId) {
        return ResponseEntity.ok(creatorService.viewAnalytics(creatorId));
    }

    @PatchMapping("/posts/{postId}/pin")
    public ResponseEntity<Void> pinPost(@PathVariable String postId) {
        creatorService.pinPost(postId);
        return ResponseEntity.ok().build();
    }
}
