package com.pesocial.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pesocial.model.analytics.CreatorAnalytics;

public interface CreatorAnalyticsRepository extends MongoRepository<CreatorAnalytics, String> {
    Optional<CreatorAnalytics> findByCreatorId(String creatorId);
}
