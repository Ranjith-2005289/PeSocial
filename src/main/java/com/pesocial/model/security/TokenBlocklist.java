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
@Document(collection = "token_blocklist")
public class TokenBlocklist {

    @Id
    private String id;

    @Field("jti")
    private String jti;

    @Field("expires_at")
    private Instant expiresAt;
}
