package com.pesocial.model.user;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TypeAlias("regularUser")
public class RegularUser extends User {

    @Field("followers_count")
    private int followersCount;

    @Field("following_count")
    private int followingCount;

    public void likePost() {
        // Domain hook for future analytics/events.
    }

    public void commentPost() {
        // Domain hook for future analytics/events.
    }

    public void sharePost() {
        // Domain hook for future analytics/events.
    }

    public void reportPost() {
        // Domain hook for moderation workflow.
    }

    @Override
    public String getUserType() {
        return "REGULAR_USER";
    }
}
