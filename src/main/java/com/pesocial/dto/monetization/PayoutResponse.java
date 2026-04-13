package com.pesocial.dto.monetization;

public record PayoutResponse(
    String creatorId,
    double paidAmount,
    double remainingBalance,
    String status
) {
}
