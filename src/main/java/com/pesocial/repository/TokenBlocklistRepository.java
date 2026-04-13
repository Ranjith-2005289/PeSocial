package com.pesocial.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pesocial.model.security.TokenBlocklist;

public interface TokenBlocklistRepository extends MongoRepository<TokenBlocklist, String> {
    boolean existsByJti(String jti);
}
