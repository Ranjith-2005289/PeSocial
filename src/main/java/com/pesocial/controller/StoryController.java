package com.pesocial.controller;

import com.pesocial.dto.StoryDetailDTO;
import com.pesocial.model.Story;
import com.pesocial.service.StoryService;
import com.pesocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stories")
@CrossOrigin("*")
public class StoryController {
  @Autowired
  private StoryService storyService;

  @Autowired
  private UserService userService;

  @PostMapping
  public ResponseEntity<Story> createStory(Authentication auth,
      @RequestBody Map<String, String> payload) {
    String userId = resolveUserId(auth);
    String mediaUrl = payload.get("mediaUrl");
    Story story = storyService.createStory(userId, mediaUrl);
    return ResponseEntity.ok(story);
  }

  @GetMapping("/active")
  public ResponseEntity<List<StoryDetailDTO>> getActiveStories(Authentication auth) {
    String userId = resolveUserId(auth);
    List<StoryDetailDTO> stories = storyService.getActiveStories(userId);
    return ResponseEntity.ok(stories);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<StoryDetailDTO>> getUserStories(Authentication auth,
      @PathVariable String userId) {
    String currentUserId = resolveUserId(auth);
    List<StoryDetailDTO> stories = storyService.getUserStories(userId, currentUserId);
    return ResponseEntity.ok(stories);
  }

  @PostMapping("/{storyId}/view")
  public ResponseEntity<Void> viewStory(Authentication auth, @PathVariable String storyId) {
    String userId = resolveUserId(auth);
    storyService.viewStory(storyId, userId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{storyId}/like")
  public ResponseEntity<Void> likeStory(Authentication auth, @PathVariable String storyId) {
    String userId = resolveUserId(auth);
    storyService.likeStory(storyId, userId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{storyId}/unlike")
  public ResponseEntity<Void> unlikeStory(Authentication auth, @PathVariable String storyId) {
    String userId = resolveUserId(auth);
    storyService.unlikeStory(storyId, userId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{storyId}/analytics")
  public ResponseEntity<StoryDetailDTO> getStoryAnalytics(Authentication auth,
      @PathVariable String storyId) {
    String userId = resolveUserId(auth);
    StoryDetailDTO analytics = storyService.getStoryAnalytics(storyId, userId);
    if (analytics == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(analytics);
  }

  @DeleteMapping("/{storyId}")
  public ResponseEntity<Void> deleteStory(Authentication auth, @PathVariable String storyId) {
    String userId = resolveUserId(auth);
    Story story = storyService.getStoryById(storyId);
    if (story == null || !story.getAuthorId().equals(userId)) {
      return ResponseEntity.status(403).build();
    }
    storyService.deleteStory(storyId);
    return ResponseEntity.ok().build();
  }

  private String resolveUserId(Authentication auth) {
    if (auth == null || auth.getName() == null) {
      return "";
    }

    String principal = auth.getName();

    if (userService.findById(principal).isPresent()) {
      return principal;
    }

    if (userService.findByHandle(principal).isPresent()) {
      return userService.findByHandle(principal).get().getId();
    }

    if (!principal.startsWith("@") && userService.findByHandle("@" + principal).isPresent()) {
      return userService.findByHandle("@" + principal).get().getId();
    }

    return principal;
  }
}
