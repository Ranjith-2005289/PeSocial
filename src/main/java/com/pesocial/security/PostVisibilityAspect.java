package com.pesocial.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.pesocial.exception.EntityNotFoundException;
import com.pesocial.model.post.Post;
import com.pesocial.repository.PostRepository;
import com.pesocial.service.PostVisibilityService;

@Aspect
@Component
public class PostVisibilityAspect {

    private final PostRepository postRepository;
    private final PostVisibilityService postVisibilityService;

    public PostVisibilityAspect(PostRepository postRepository, PostVisibilityService postVisibilityService) {
        this.postRepository = postRepository;
        this.postVisibilityService = postVisibilityService;
    }

    @Before("@annotation(checkVisibility)")
    public void verifyVisibility(JoinPoint joinPoint, CheckVisibility checkVisibility) {
        String postId = extractPostId(joinPoint, checkVisibility.postIdParam());
        if (postId == null || postId.isBlank()) {
            return;
        }

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        String viewerId = currentViewerId();
        postVisibilityService.assertCanAccessPost(viewerId, post);
    }

    private String extractPostId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            if (paramName.equals(parameterNames[i]) && args[i] instanceof String postId) {
                return postId;
            }
        }

        return null;
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
