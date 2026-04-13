package com.pesocial.model.analytics;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "creator_analytics")
public class CreatorAnalytics {

    @Id
    private String id;

    @Field("creator_id")
    private String creatorId;

    @Field("total_views")
    private long totalViews;

    @Field("total_likes")
    private long totalLikes;

    @Field("total_shares")
    private long totalShares;

    @Field("followers_count")
    private long followersCount;

    @Field("engagement_rate")
    private double engagementRate;

    public void updateMetrics(long totalViews, long totalLikes, long totalShares, long followersCount) {
        this.totalViews = totalViews;
        this.totalLikes = totalLikes;
        this.totalShares = totalShares;
        this.followersCount = followersCount;
        this.engagementRate = calculateEngagement();
    }

    public double calculateEngagement() {
        if (followersCount <= 0) {
            return 0.0;
        }
        return ((double) (totalLikes + totalShares) / followersCount) * 100;
    }
}
