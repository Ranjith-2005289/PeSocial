package com.pesocial.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pesocial.dto.monetization.MonetizationResult;
import com.pesocial.dto.monetization.PayoutResponse;
import com.pesocial.service.MonetizationService;

@RestController
@RequestMapping("/api/monetization")
public class MonetizationController {

    private final MonetizationService monetizationService;

    public MonetizationController(MonetizationService monetizationService) {
        this.monetizationService = monetizationService;
    }

    @GetMapping("/{creatorId}/eligibility")
    public ResponseEntity<Boolean> eligibility(@PathVariable String creatorId) {
        return ResponseEntity.ok(monetizationService.validateMonetizationEligibility(creatorId));
    }

    @PostMapping("/{creatorId}/revenue")
    public ResponseEntity<MonetizationResult> applyRevenue(@PathVariable String creatorId,
                                                           @RequestParam double grossRevenue) {
        return ResponseEntity.ok(monetizationService.applyMonetizationRules(creatorId, grossRevenue));
    }

    @GetMapping("/{creatorId}/balance")
    public ResponseEntity<Double> currentBalance(@PathVariable String creatorId) {
        return ResponseEntity.ok(monetizationService.getCurrentBalance(creatorId));
    }

    @PatchMapping("/{creatorId}/payout")
    public ResponseEntity<PayoutResponse> payout(@PathVariable String creatorId) {
        return ResponseEntity.ok(monetizationService.executePayout(creatorId));
    }
}
