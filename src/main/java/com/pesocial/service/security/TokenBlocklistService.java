package com.pesocial.service.security;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.pesocial.model.security.TokenBlocklist;
import com.pesocial.repository.TokenBlocklistRepository;

@Service
public class TokenBlocklistService {

    private final TokenBlocklistRepository tokenBlocklistRepository;

    public TokenBlocklistService(TokenBlocklistRepository tokenBlocklistRepository) {
        this.tokenBlocklistRepository = tokenBlocklistRepository;
    }

    public void blacklist(String jti, Instant expiresAt) {
        TokenBlocklist blocked = new TokenBlocklist();
        blocked.setJti(jti);
        blocked.setExpiresAt(expiresAt);
        tokenBlocklistRepository.save(blocked);
    }

    public boolean isBlacklisted(String jti) {
        return tokenBlocklistRepository.existsByJti(jti);
    }
}
