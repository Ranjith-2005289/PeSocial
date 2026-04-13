package com.pesocial.model.user;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import com.pesocial.model.analytics.CreatorAnalytics;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TypeAlias("creator")
public class Creator extends User {

    @Field("creator_id")
    private String creatorId;

    @Field("verification_status")
    private boolean verificationStatus;

    @Field("monetization_enabled")
    private boolean monetizationEnabled;

    @Field("total_earnings")
    private double totalEarnings;

    @Field("followers_count")
    private int followersCount;

    @Field("following_count")
    private int followingCount;

    @DocumentReference(lazy = true)
    @Field("analytics")
    private CreatorAnalytics analytics;

    public void enableMonetization() {
        this.monetizationEnabled = true;
    }

    public void uploadExclusivePost() {
        // Domain hook for exclusive content workflow.
    }

    public void pinPost() {
        // Domain hook for pinned posts.
    }

    @Override
    public String getUserType() {
        return "CREATOR";
    }
}
