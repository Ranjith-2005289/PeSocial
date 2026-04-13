package com.pesocial.repository;

import java.time.Instant;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pesocial.model.subscription.Subscription;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    boolean existsBySubscriberIdAndCreatorIdAndActiveIsTrueAndEndAtAfter(String subscriberId, String creatorId, Instant now);
}
