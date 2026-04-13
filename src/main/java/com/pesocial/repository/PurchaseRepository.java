package com.pesocial.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pesocial.model.subscription.Purchase;

public interface PurchaseRepository extends MongoRepository<Purchase, String> {
    boolean existsByBuyerIdAndCreatorIdAndActiveIsTrue(String buyerId, String creatorId);
    boolean existsByBuyerIdAndPostIdAndActiveIsTrue(String buyerId, String postId);
}
