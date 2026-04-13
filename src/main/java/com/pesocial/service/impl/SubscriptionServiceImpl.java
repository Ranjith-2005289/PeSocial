package com.pesocial.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.pesocial.repository.PurchaseRepository;
import com.pesocial.repository.SubscriptionRepository;
import com.pesocial.service.SubscriptionService;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final PurchaseRepository purchaseRepository;
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionServiceImpl(PurchaseRepository purchaseRepository,
                                   SubscriptionRepository subscriptionRepository) {
        this.purchaseRepository = purchaseRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public boolean hasActiveAccessToCreator(String viewerId, String creatorId) {
        if (viewerId == null || viewerId.isBlank()) {
            return false;
        }

        boolean hasSubscription = subscriptionRepository
            .existsBySubscriberIdAndCreatorIdAndActiveIsTrueAndEndAtAfter(viewerId, creatorId, Instant.now());
        boolean hasCreatorPurchase = purchaseRepository
            .existsByBuyerIdAndCreatorIdAndActiveIsTrue(viewerId, creatorId);

        return hasSubscription || hasCreatorPurchase;
    }

    @Override
    public boolean hasActiveAccessToPost(String viewerId, String creatorId, String postId) {
        if (viewerId == null || viewerId.isBlank()) {
            return false;
        }

        boolean hasPostPurchase = purchaseRepository
            .existsByBuyerIdAndPostIdAndActiveIsTrue(viewerId, postId);

        return hasPostPurchase || hasActiveAccessToCreator(viewerId, creatorId);
    }
}
