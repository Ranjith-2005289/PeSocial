package com.pesocial.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pesocial.model.post.Post;

public interface PostRepository extends MongoRepository<Post, String>, PostQueryRepository {
    List<Post> findByAuthorIdOrderByCreatedAtDesc(String authorId);
}
