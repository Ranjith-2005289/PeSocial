package com.pesocial.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pesocial.dto.post.CreatePostRequest;
import com.pesocial.dto.post.EditPostRequest;
import com.pesocial.dto.post.PostResponseDto;
import com.pesocial.model.post.Post;
import com.pesocial.security.CheckVisibility;
import com.pesocial.security.TargetEntity;
import com.pesocial.service.OwnershipService;
import com.pesocial.service.PostService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final OwnershipService ownershipService;

    public PostController(PostService postService, OwnershipService ownershipService) {
        this.postService = postService;
        this.ownershipService = ownershipService;
    }

    @PostMapping
    public ResponseEntity<PostResponseDto> create(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.toResponse(postService.createPost(request)));
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> feed() {
        return ResponseEntity.ok(postService.toResponseList(postService.getFeedPosts(currentViewerId())));
    }

    @GetMapping("/{postId}")
    @CheckVisibility
    public ResponseEntity<PostResponseDto> getById(@PathVariable String postId) {
        return ResponseEntity.ok(postService.toResponse(postService.getPostById(postId)));
    }

    @PatchMapping("/{postId}")
    @CheckVisibility
    public ResponseEntity<PostResponseDto> edit(@PathVariable String postId, @RequestBody EditPostRequest request) {
        Post post = postService.getPostById(postId);
        ownershipService.assertOwnership(TargetEntity.POST, post.getAuthorId(), false);
        return ResponseEntity.ok(postService.toResponse(postService.editPost(postId, request)));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@PathVariable String postId) {
        postService.deletePost(postId, currentViewerId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}/like")
    @CheckVisibility
    public ResponseEntity<PostResponseDto> like(@PathVariable String postId) {
        return ResponseEntity.ok(postService.toResponse(postService.addLike(postId, currentViewerId())));
    }

    @PatchMapping("/{postId}/unlike")
    @CheckVisibility
    public ResponseEntity<PostResponseDto> unlike(@PathVariable String postId) {
        return ResponseEntity.ok(postService.toResponse(postService.removeLike(postId)));
    }

    @PatchMapping("/{postId}/comment")
    @CheckVisibility
    public ResponseEntity<PostResponseDto> comment(@PathVariable String postId, @RequestParam String value) {
        return ResponseEntity.ok(postService.toResponse(postService.addComment(postId, value, currentViewerId())));
    }

    @PatchMapping("/{postId}/share")
    @CheckVisibility
    public ResponseEntity<PostResponseDto> share(@PathVariable String postId) {
        return ResponseEntity.ok(postService.toResponse(postService.sharePost(postId)));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<PostResponseDto>> byAuthor(@PathVariable String authorId) {
        return ResponseEntity.ok(postService.toResponseList(postService.getUserPosts(authorId, currentViewerId())));
    }

    private String currentViewerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String principalString && !"anonymousUser".equals(principalString)) {
            return principalString;
        }
        return null;
    }
}
