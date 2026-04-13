package com.pesocial.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "stories")
public class Story {
  @Id
  private String id;
  private String authorId;
  private String mediaUrl;
  
  @Indexed(expireAfterSeconds = 86400)
  private LocalDateTime timestamp;
  
  private List<String> viewers = new ArrayList<>();
  private List<String> likes = new ArrayList<>();

  public Story() {
  }

  public Story(String authorId, String mediaUrl) {
    this.authorId = authorId;
    this.mediaUrl = mediaUrl;
    this.timestamp = LocalDateTime.now();
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
}
