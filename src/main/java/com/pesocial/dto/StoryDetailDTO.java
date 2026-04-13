package com.pesocial.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StoryDetailDTO {
  private String id;
  private String authorId;
  private String mediaUrl;
  private LocalDateTime timestamp;
  private int viewerCount;
  private int likeCount;
  private List<String> viewers;
  private List<String> likes;
  private boolean viewed;

  public StoryDetailDTO() {
  }

  public StoryDetailDTO(String id, String authorId, String mediaUrl, LocalDateTime timestamp,
      List<String> viewers, List<String> likes, boolean viewed) {
    this.id = id;
    this.authorId = authorId;
    this.mediaUrl = mediaUrl;
    this.timestamp = timestamp;
    this.viewers = viewers;
    this.likes = likes;
    this.viewerCount = viewers != null ? viewers.size() : 0;
    this.likeCount = likes != null ? likes.size() : 0;
    this.viewed = viewed;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAuthorId() {
    return authorId;
  }

  public void setAuthorId(String authorId) {
    this.authorId = authorId;
  }

  public String getMediaUrl() {
    return mediaUrl;
  }

  public void setMediaUrl(String mediaUrl) {
    this.mediaUrl = mediaUrl;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public int getViewerCount() {
    return viewerCount;
  }

  public void setViewerCount(int viewerCount) {
    this.viewerCount = viewerCount;
  }

  public int getLikeCount() {
    return likeCount;
  }

  public void setLikeCount(int likeCount) {
    this.likeCount = likeCount;
  }

  public List<String> getViewers() {
    return viewers;
  }

  public void setViewers(List<String> viewers) {
    this.viewers = viewers;
  }

  public List<String> getLikes() {
    return likes;
  }

  public void setLikes(List<String> likes) {
    this.likes = likes;
  }

  public boolean isViewed() {
    return viewed;
  }

  public void setViewed(boolean viewed) {
    this.viewed = viewed;
  }
}
