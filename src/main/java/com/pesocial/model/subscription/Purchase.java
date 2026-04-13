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
@Document(collection = "purchases")
public class Purchase {

    @Id
    private String id;

    @Field("buyer_id")
    private String buyerId;

    @Field("creator_id")
    private String creatorId;

    @Field("post_id")
    private String postId;

    @Field("active")
    private boolean active;

    @Field("purchased_at")
    private Instant purchasedAt = Instant.now();
}
