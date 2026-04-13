package com.pesocial.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.pesocial.model.post.Post;
import com.pesocial.model.user.User;
import com.pesocial.service.SubscriptionService;

@Repository
public class PostQueryRepositoryImpl implements PostQueryRepository {

    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    public PostQueryRepositoryImpl(MongoTemplate mongoTemplate,
                                   UserRepository userRepository,
                                   SubscriptionService subscriptionService) {
        this.mongoTemplate = mongoTemplate;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public List<Post> findVisibleByAuthorForViewer(String authorId, String viewerId) {
        List<String> allowedVisibility = allowedVisibilityForAuthor(authorId, viewerId);

        Query query = new Query();
        query.addCriteria(Criteria.where("author_id").is(authorId));
        query.addCriteria(Criteria.where("visibility").in(allowedVisibility));
        query.with(Sort.by(Sort.Direction.DESC, "created_at"));

        return mongoTemplate.find(query, Post.class);
    }

    @Override
    public List<Post> findVisibleForViewer(String viewerId) {
        List<Criteria> visibilityCriteria = new ArrayList<>();
        visibilityCriteria.add(Criteria.where("visibility").is("PUBLIC"));

        if (viewerId != null && !viewerId.isBlank()) {
            visibilityCriteria.add(Criteria.where("author_id").is(viewerId));

            User viewer = userRepository.findById(viewerId).orElse(null);
            if (viewer != null && !viewer.getFollowing().isEmpty()) {
                visibilityCriteria.add(new Criteria().andOperator(
                    Criteria.where("author_id").in(viewer.getFollowing()),
                    Criteria.where("visibility").is("FOLLOWERS")
                ));
            }
        }

        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(visibilityCriteria.toArray(new Criteria[0])));
        query.with(Sort.by(Sort.Direction.DESC, "created_at"));

        return mongoTemplate.find(query, Post.class)
            .stream()
            .filter(post -> !"EXCLUSIVE".equalsIgnoreCase(post.getVisibility())
                || subscriptionService.hasActiveAccessToPost(viewerId, post.getAuthorId(), post.getId())
                || (viewerId != null && viewerId.equals(post.getAuthorId())))
            .toList();
    }

    private List<String> allowedVisibilityForAuthor(String authorId, String viewerId) {
        List<String> allowed = new ArrayList<>();
        allowed.add("PUBLIC");

        if (viewerId == null || viewerId.isBlank()) {
            return allowed;
        }

        if (viewerId.equals(authorId)) {
            allowed.add("PRIVATE");
            allowed.add("FOLLOWERS");
            allowed.add("EXCLUSIVE");
            return allowed;
        }

        User author = userRepository.findById(authorId).orElse(null);
        if (author != null && author.getFollowers().contains(viewerId)) {
            allowed.add("FOLLOWERS");
        }

        if (subscriptionService.hasActiveAccessToCreator(viewerId, authorId)) {
            allowed.add("EXCLUSIVE");
        }

        return allowed;
    }
}
