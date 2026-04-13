package com.pesocial.dto.monetization;

public record MonetizationResult(
    String creatorId,
    double grossRevenue,
    double platformCommission,
    double netRevenue,
    boolean payoutEligible,
    String message
) {
}
