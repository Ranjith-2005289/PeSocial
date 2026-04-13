package com.pesocial.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pesocial.model.security.RefreshToken;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByJti(String jti);
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(String userId);
}
