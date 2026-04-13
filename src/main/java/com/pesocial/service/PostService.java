package com.pesocial.service;

import java.util.List;

import com.pesocial.dto.post.CreatePostRequest;
import com.pesocial.dto.post.EditPostRequest;
import com.pesocial.dto.post.PostResponseDto;
import com.pesocial.model.post.Post;

public interface PostService {
    Post createPost(CreatePostRequest request);
    List<Post> getFeedPosts(String viewerId);
    Post getPostById(String postId);
    Post editPost(String postId, EditPostRequest request);
    void deletePost(String postId, String userId);
    Post addLike(String postId, String currentUserId);
    Post removeLike(String postId);
    Post addComment(String postId, String comment, String currentUserId);
    Post sharePost(String postId);
    List<Post> getUserPosts(String authorId, String viewerId);
    PostResponseDto toResponse(Post post);
    List<PostResponseDto> toResponseList(List<Post> posts);
}
