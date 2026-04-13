package com.pesocial.service;

import com.pesocial.dto.monetization.MonetizationResult;
import com.pesocial.dto.monetization.PayoutResponse;

public interface MonetizationService {
    boolean validateMonetizationEligibility(String creatorId);
    MonetizationResult applyMonetizationRules(String creatorId, double grossRevenue);
    MonetizationResult calculateCreatorRevenue(String creatorId, double grossRevenue);
    double enforcePlatformCommission(double grossRevenue);
    PayoutResponse executePayout(String creatorId);
    double getCurrentBalance(String creatorId);
}
