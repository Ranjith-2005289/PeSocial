package com.pesocial.model.security;

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
@Document(collection = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    @Field("jti")
    private String jti;

    @Field("user_id")
    private String userId;

    @Field("role")
    private String role;

    @Field("token")
    private String token;

    @Field("expires_at")
    private Instant expiresAt;

    @Field("revoked")
    private boolean revoked;
}
