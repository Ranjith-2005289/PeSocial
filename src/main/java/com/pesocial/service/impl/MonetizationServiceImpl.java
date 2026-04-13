package com.pesocial.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pesocial.dto.monetization.MonetizationResult;
import com.pesocial.dto.monetization.PayoutResponse;
import com.pesocial.model.user.Creator;
import com.pesocial.model.user.User;
import com.pesocial.repository.UserRepository;
import com.pesocial.service.MonetizationService;

@Service
public class MonetizationServiceImpl implements MonetizationService {

    private final UserRepository userRepository;
    private final double platformCommissionRate;
    private final double payoutThreshold;

    public MonetizationServiceImpl(UserRepository userRepository,
                                   @Value("${app.monetization.platform-commission:0.20}") double platformCommissionRate,
                                   @Value("${app.monetization.payout-threshold:50}") double payoutThreshold) {
        this.userRepository = userRepository;
        this.platformCommissionRate = platformCommissionRate;
        this.payoutThreshold = payoutThreshold;
    }

    @Override
    public boolean validateMonetizationEligibility(String creatorId) {
        User user = userRepository.findById(creatorId)
            .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        if (!(user instanceof Creator creator)) {
            return false;
        }
        return creator.isVerificationStatus() && creator.isMonetizationEnabled();
    }

    @Override
    public MonetizationResult applyMonetizationRules(String creatorId, double grossRevenue) {
        return calculateCreatorRevenue(creatorId, grossRevenue);
    }

    @Override
    public MonetizationResult calculateCreatorRevenue(String creatorId, double grossRevenue) {
        if (!validateMonetizationEligibility(creatorId)) {
            return new MonetizationResult(
                creatorId,
                grossRevenue,
                0.0,
                0.0,
                false,
                "Creator is not eligible for monetization"
            );
        }

        User user = userRepository.findById(creatorId)
            .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        Creator creator = (Creator) user;

        double commission = enforcePlatformCommission(grossRevenue);
        double netRevenue = Math.max(0, grossRevenue - commission);
        creator.setTotalEarnings(creator.getTotalEarnings() + netRevenue);
        userRepository.save(creator);

        boolean payoutEligible = creator.getTotalEarnings() >= payoutThreshold;
        String message = payoutEligible
            ? "Payout threshold reached"
            : "Payout threshold not reached";

        return new MonetizationResult(
            creatorId,
            grossRevenue,
            commission,
            netRevenue,
            payoutEligible,
            message
        );
    }

    @Override
    public double enforcePlatformCommission(double grossRevenue) {
        return grossRevenue * platformCommissionRate;
    }

    @Override
    public PayoutResponse executePayout(String creatorId) {
        User user = userRepository.findById(creatorId)
            .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        if (!(user instanceof Creator creator)) {
            throw new IllegalArgumentException("User is not a creator");
        }

        if (!validateMonetizationEligibility(creatorId)) {
            return new PayoutResponse(creatorId, 0.0, creator.getTotalEarnings(), "NOT_ELIGIBLE");
        }

        double balance = creator.getTotalEarnings();
        if (balance < payoutThreshold) {
            return new PayoutResponse(creatorId, 0.0, balance, "THRESHOLD_NOT_REACHED");
        }

        creator.setTotalEarnings(0.0);
        userRepository.save(creator);
        return new PayoutResponse(creatorId, balance, creator.getTotalEarnings(), "PAID");
    }

    @Override
    public double getCurrentBalance(String creatorId) {
        User user = userRepository.findById(creatorId)
            .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        if (!(user instanceof Creator creator)) {
            throw new IllegalArgumentException("User is not a creator");
        }
        return creator.getTotalEarnings();
    }
}
