package com.pesocial.service.impl;

import com.pesocial.dto.StoryDetailDTO;
import com.pesocial.model.Story;
import com.pesocial.repository.StoryRepository;
import com.pesocial.service.StoryService;
import com.pesocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoryServiceImpl implements StoryService {
  @Autowired
  private StoryRepository storyRepository;

  @Autowired
  private UserService userService;

  @Override
  public Story createStory(String authorId, String mediaUrl) {
    Story story = new Story(authorId, mediaUrl);
    return storyRepository.save(story);
  }

  @Override
  public Story getStoryById(String storyId) {
    return storyRepository.findById(storyId).orElse(null);
  }

  @Override
  public List<StoryDetailDTO> getActiveStories(String currentUserId) {
    LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
    
    // Get list of users that current user follows + self
    List<String> followingIds = userService.getFollowingIds(currentUserId);
    followingIds.add(currentUserId);

    List<Story> activeStories = storyRepository.findActiveStoriesByAuthors(followingIds, twentyFourHoursAgo);

    return activeStories.stream()
        .map(story -> new StoryDetailDTO(
            story.getId(),
            story.getAuthorId(),
            story.getMediaUrl(),
            story.getTimestamp(),
            story.getViewers(),
            story.getLikes(),
            story.getViewers().contains(currentUserId)))
        .collect(Collectors.toList());
  }

  @Override
  public void viewStory(String storyId, String userId) {
    Story story = storyRepository.findById(storyId).orElse(null);
    if (story != null && !story.getViewers().contains(userId)) {
      story.getViewers().add(userId);
      storyRepository.save(story);
    }
  }

  @Override
  public void likeStory(String storyId, String userId) {
    Story story = storyRepository.findById(storyId).orElse(null);
    if (story != null && !story.getLikes().contains(userId)) {
      story.getLikes().add(userId);
      storyRepository.save(story);
    }
  }

  @Override
  public void unlikeStory(String storyId, String userId) {
    Story story = storyRepository.findById(storyId).orElse(null);
    if (story != null) {
      story.getLikes().remove(userId);
      storyRepository.save(story);
    }
  }

  @Override
  public StoryDetailDTO getStoryAnalytics(String storyId, String currentUserId) {
    Story story = storyRepository.findById(storyId).orElse(null);
    if (story == null) {
      return null;
    }

    // Only author can see full analytics
    if (!story.getAuthorId().equals(currentUserId)) {
      return null;
    }

    return new StoryDetailDTO(
        story.getId(),
        story.getAuthorId(),
        story.getMediaUrl(),
        story.getTimestamp(),
        story.getViewers(),
        story.getLikes(),
        story.getViewers().contains(currentUserId));
  }

  @Override
  public List<StoryDetailDTO> getUserStories(String userId, String currentUserId) {
    LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
    List<Story> userStories = storyRepository.findByAuthorIdAndTimestampGreaterThan(userId, twentyFourHoursAgo);

    return userStories.stream()
        .map(story -> new StoryDetailDTO(
            story.getId(),
            story.getAuthorId(),
            story.getMediaUrl(),
            story.getTimestamp(),
            story.getViewers(),
            story.getLikes(),
            story.getViewers().contains(currentUserId)))
        .collect(Collectors.toList());
  }

  @Override
  public void deleteStory(String storyId) {
    storyRepository.deleteById(storyId);
  }
}
