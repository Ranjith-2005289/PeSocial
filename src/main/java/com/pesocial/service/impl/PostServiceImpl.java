package com.pesocial.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import com.pesocial.dto.post.CreatePostRequest;
import com.pesocial.dto.post.EditPostRequest;
import com.pesocial.dto.post.PostResponseDto;
import com.pesocial.exception.AccessDeniedException;
import com.pesocial.exception.EntityNotFoundException;
import com.pesocial.model.post.Post;
import com.pesocial.model.user.User;
import com.pesocial.model.user.UserRole;
import com.pesocial.repository.PostRepository;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.NotificationService;
import com.pesocial.service.PostService;
import com.pesocial.service.feed.FeedPublisher;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final FeedPublisher feedPublisher;
    private final UserRepository userRepository;
    private final GridFsTemplate gridFsTemplate;
    private final NotificationService notificationService;

    public PostServiceImpl(PostRepository postRepository,
                           FeedPublisher feedPublisher,
                           UserRepository userRepository,
                           GridFsTemplate gridFsTemplate,
                           NotificationService notificationService) {
        this.postRepository = postRepository;
        this.feedPublisher = feedPublisher;
        this.userRepository = userRepository;
        this.gridFsTemplate = gridFsTemplate;
        this.notificationService = notificationService;
    }

    @Override
    public Post createPost(CreatePostRequest request) {
        Post post = new Post();
        post.setAuthorId(request.authorId());
        post.setContentText(request.contentText());
        post.setMedia(request.media());
        post.setMediaType(request.mediaType());
        if (request.visibility() != null && !request.visibility().isBlank()) {
            post.setVisibility(request.visibility());
        }

        Post saved = postRepository.save(post);
        feedPublisher.publishPost(saved.getAuthorId(), saved.getId());
        return saved;
    }

    @Override
    public List<Post> getFeedPosts(String viewerId) {
        return postRepository.findVisibleForViewer(viewerId);
    }

    @Override
    public Post getPostById(String postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    @Override
    public Post editPost(String postId, EditPostRequest request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        post.editPost(request.contentText(), request.media(), request.mediaType(), request.visibility());
        return postRepository.save(post);
    }

    @Override
    public void deletePost(String postId, String userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        boolean isAuthor = post.getAuthorId().equals(userId);
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to delete this post");
        }

        if (post.getMedia() != null && post.getMedia().getGridFsFileId() != null && !post.getMedia().getGridFsFileId().isBlank()) {
            Object fileId = toObjectIdOrString(post.getMedia().getGridFsFileId());
            gridFsTemplate.delete(new Query(Criteria.where("_id").is(fileId)));
        }

        postRepository.deleteById(postId);
    }

    @Override
    public Post addLike(String postId, String currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        boolean newlyLiked = post.addLike(currentUserId);
        Post saved = newlyLiked ? postRepository.save(post) : post;

        // Get current user (who is liking) and post author (recipient)
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        User author = userRepository.findById(saved.getAuthorId()).orElse(null);
        
        // Send notification to post author about who liked their post
        if (newlyLiked && currentUser != null && author != null && !currentUserId.equals(author.getId())) {
            String senderHandle = currentUser.getHandle() != null ? currentUser.getHandle() : currentUser.getUsername();
            notificationService.sendLikeNotification(author.getId(), senderHandle, saved.getId());
        }

        return saved;
    }

    @Override
    public Post removeLike(String postId, String currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        boolean removed = post.removeLike(currentUserId);
        return removed ? postRepository.save(post) : post;
    }

    @Override
    public Post addComment(String postId, String comment, String currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        post.addComment(comment);
        Post saved = postRepository.save(post);

        // Get current user (who is commenting) and post author (recipient)
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        User author = userRepository.findById(saved.getAuthorId()).orElse(null);
        
        // Send notification to post author about who commented on their post
        if (currentUser != null && author != null && !currentUserId.equals(author.getId())) {
            String senderHandle = currentUser.getHandle() != null ? currentUser.getHandle() : currentUser.getUsername();
            notificationService.sendCommentNotification(author.getId(), senderHandle, saved.getId(), comment);
        }

        return saved;
    }

    @Override
    public Post sharePost(String postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        post.sharePost();
        return postRepository.save(post);
    }

    @Override
    public List<Post> getUserPosts(String authorId, String viewerId) {
        return postRepository.findVisibleByAuthorForViewer(authorId, viewerId);
    }

    @Override
    public PostResponseDto toResponse(Post post) {
        String authorName = userRepository.findById(post.getAuthorId())
            .map(user -> user.getUsername())
            .orElse(post.getAuthorId());

        String mediaId = post.getMedia() != null ? post.getMedia().getGridFsFileId() : null;

        return new PostResponseDto(
            post.getId(),
            post.getAuthorId(),
            authorName,
            post.getContentText(),
            post.getMedia(),
            mediaId,
            post.getMediaType(),
            post.getVisibility(),
            post.getLikesCount(),
            post.getSharesCount(),
            post.getComments(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }

    @Override
    public List<PostResponseDto> toResponseList(List<Post> posts) {
        return posts.stream().map(this::toResponse).toList();
    }

    private Object toObjectIdOrString(String fileId) {
        if (ObjectId.isValid(fileId)) {
            return new ObjectId(fileId);
        }
        return fileId;
    }
}
