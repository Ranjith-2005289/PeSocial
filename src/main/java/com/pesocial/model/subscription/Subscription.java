package com.pesocial.model.subscription;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "subscriptions")
public class Subscription {

    @Id
    private String id;

    @Field("subscriber_id")
    private String subscriberId;

    @Field("creator_id")
    private String creatorId;

    @Field("active")
    private boolean active;

    @Field("start_at")
    private Instant startAt = Instant.now();

    @Field("end_at")
    private Instant endAt;
}
