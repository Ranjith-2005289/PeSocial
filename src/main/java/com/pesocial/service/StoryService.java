package com.pesocial.service;

import com.pesocial.dto.StoryDetailDTO;
import com.pesocial.model.Story;
import java.util.List;

public interface StoryService {
  Story createStory(String authorId, String mediaUrl);

  Story getStoryById(String storyId);

  List<StoryDetailDTO> getActiveStories(String currentUserId);

  void viewStory(String storyId, String userId);

  void likeStory(String storyId, String userId);

  void unlikeStory(String storyId, String userId);

  StoryDetailDTO getStoryAnalytics(String storyId, String currentUserId);

  List<StoryDetailDTO> getUserStories(String userId, String currentUserId);

  void deleteStory(String storyId);
}
