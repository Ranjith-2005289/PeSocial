package com.pesocial.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pesocial.dto.auth.AuthResponse;
import com.pesocial.dto.user.MyProfileDto;
import com.pesocial.dto.user.UpdateMyProfileRequest;
import com.pesocial.dto.user.UserProfileDto;
import com.pesocial.dto.user.UserSummaryDto;
import com.pesocial.exception.AccessDeniedException;
import com.pesocial.exception.EntityNotFoundException;
import com.pesocial.model.post.Post;
import com.pesocial.model.user.User;
import com.pesocial.service.PostService;
import com.pesocial.service.UserService;
import com.pesocial.service.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final GridFsTemplate gridFsTemplate;
    private final JwtService jwtService;
    private static final String BEARER_PREFIX = "Bearer ";

    public UserController(UserService userService,
                          PostService postService,
                          GridFsTemplate gridFsTemplate,
                          JwtService jwtService) {
        this.userService = userService;
        this.postService = postService;
        this.gridFsTemplate = gridFsTemplate;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public ResponseEntity<MyProfileDto> getMyProfile(HttpServletRequest request) {
        String principalName = extractUserIdFromToken(request).orElseGet(this::currentAuthenticatedName);
        User user = userService.findById(principalName)
            .or(() -> userService.findByHandle(principalName))
            .orElseThrow(() -> new AccessDeniedException("Invalid session. Please login again."));
        String userId = user.getId();
        List<Post> posts = postService.getUserPosts(userId, userId);

        return ResponseEntity.ok(new MyProfileDto(
            user.getId(),
            user.getUsername(),
            user.getHandle(),
            user.getEmail(),
            user.getProfilePhoto(),
            user.getBio(),
            user.getRole().name(),
            user.getFollowers().size(),
            user.getFollowing().size(),
            postService.toResponseList(posts)
        ));
    }

    @PutMapping("/update")
    public ResponseEntity<UserProfileDto> updateMyProfile(@Valid @org.springframework.web.bind.annotation.RequestBody UpdateMyProfileRequest request) {
        User updated = userService.updateMyProfile(currentUserId(), request.username(), request.handle(), request.bio());
        return ResponseEntity.ok(userService.getUserProfile(updated.getId()));
    }

    @PostMapping(value = "/profile-pic", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadProfilePhoto(@RequestParam("file") MultipartFile file) {
        return uploadProfilePhotoInternal(file);
    }

    @PostMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        return uploadProfilePhotoInternal(file);
    }

    private ResponseEntity<Map<String, String>> uploadProfilePhotoInternal(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Profile image file is required");
        }

        String userId = currentUserId();
        User existing = userService.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (existing.getProfilePhoto() != null && existing.getProfilePhoto().startsWith("/api/media/")) {
            String existingId = existing.getProfilePhoto().substring("/api/media/".length());
            gridFsTemplate.delete(new Query(Criteria.where("_id").is(toObjectIdOrString(existingId))));
        }

        Object fileId;
        try {
            Document metadata = new Document();
            metadata.put("contentType", file.getContentType());
            metadata.put("ownerUserId", userId);
            fileId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), metadata);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to store profile image", ex);
        }

        String mediaPath = "/api/media/" + fileId;
        userService.updateProfilePhoto(userId, mediaPath);
        return ResponseEntity.ok(Map.of("profilePhoto", mediaPath));
    }

    @PostMapping("/{followerId}/follow/{followeeId}")
    public ResponseEntity<Void> follow(@PathVariable String followerId, @PathVariable String followeeId) {
        userService.follow(followerId, followeeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/follow/{handle}")
    public ResponseEntity<Void> followByHandle(@PathVariable String handle) {
        userService.followByHandle(currentUserId(), handle);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/follow/{handle}")
    public ResponseEntity<Void> followByHandleAlias(@PathVariable String handle) {
        userService.followByHandle(currentUserId(), handle);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/unfollow/{handle}")
    public ResponseEntity<Void> unfollowByHandle(@PathVariable String handle) {
        userService.unfollowByHandle(currentUserId(), handle);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/remove-follower/{handle}")
    public ResponseEntity<Void> removeFollower(@PathVariable String handle) {
        userService.removeFollowerByHandle(currentUserId(), handle);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{followerId}/unfollow/{followeeId}")
    public ResponseEntity<Void> unfollow(@PathVariable String followerId, @PathVariable String followeeId) {
        userService.unfollow(followerId, followeeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfileDto>> search(@RequestParam String handle) {
        return ResponseEntity.ok(userService.searchUserProfiles(handle));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> profile(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @GetMapping("/handle/{handle}")
    public ResponseEntity<UserProfileDto> profileByHandle(@PathVariable String handle) {
        return ResponseEntity.ok(userService.getUserProfileByHandle(handle));
    }

    @GetMapping("/{handle}/followers")
    public ResponseEntity<List<UserSummaryDto>> followers(@PathVariable String handle,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getFollowersByHandle(handle, page, size));
    }

    @GetMapping("/me/followers")
    public ResponseEntity<List<UserSummaryDto>> myFollowers(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        String userId = currentUserId();
        User user = userService.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(userService.getFollowersByHandle(user.getHandle(), page, size));
    }

    @GetMapping("/{handle}/following")
    public ResponseEntity<List<UserSummaryDto>> following(@PathVariable String handle,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getFollowingByHandle(handle, page, size));
    }

    @GetMapping("/me/following")
    public ResponseEntity<List<UserSummaryDto>> myFollowing(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        String userId = currentUserId();
        User user = userService.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(userService.getFollowingByHandle(user.getHandle(), page, size));
    }

    @GetMapping("/{userId}/feed")
    public ResponseEntity<List<Post>> feed(@PathVariable String userId) {
        return ResponseEntity.ok(userService.viewFeed(userId));
    }

    @PatchMapping("/{userId}/profile")
    public ResponseEntity<User> updateProfile(@PathVariable String userId,
                                              @RequestParam String username,
                                              @RequestParam(required = false) String profilePhoto,
                                              @RequestParam(required = false) String bio) {
        return ResponseEntity.ok(userService.updateProfile(userId, username, profilePhoto, bio));
    }

    @PostMapping("/me/become-creator")
    public ResponseEntity<AuthResponse> becomeCreator() {
        return ResponseEntity.ok(userService.becomeCreator(currentUserId()));
    }

    private Object toObjectIdOrString(String fileId) {
        if (ObjectId.isValid(fileId)) {
            return new ObjectId(fileId);
        }
        return fileId;
    }

    private String currentAuthenticatedName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String principalString && !"anonymousUser".equals(principalString)) {
            return principalString;
        }

        throw new IllegalStateException("User not authenticated");
    }

    private String currentUserId() {
        String principalName = currentAuthenticatedName();
        return userService.findById(principalName)
            .or(() -> userService.findByHandle(principalName))
            .map(User::getId)
            .orElseThrow(() -> new AccessDeniedException("Invalid session. Please login again."));
    }

    private Optional<String> extractUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            return Optional.ofNullable(jwtService.extractUserId(token));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
