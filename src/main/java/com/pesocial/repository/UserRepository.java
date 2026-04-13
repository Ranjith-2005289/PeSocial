package com.pesocial.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pesocial.model.user.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByHandle(String handle);
    boolean existsByEmail(String email);
    boolean existsByHandle(String handle);
    List<User> findByHandleContainingIgnoreCase(String handle);
    List<User> findByUsernameContainingIgnoreCase(String query);
}
