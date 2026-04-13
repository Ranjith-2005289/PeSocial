package com.pesocial.service.impl;

import org.springframework.stereotype.Service;

import com.pesocial.exception.AccessDeniedException;
import com.pesocial.security.SecurityUtils;
import com.pesocial.security.TargetEntity;
import com.pesocial.service.OwnershipService;

@Service
public class OwnershipServiceImpl implements OwnershipService {

    private final SecurityUtils securityUtils;

    public OwnershipServiceImpl(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @Override
    public void assertOwnership(TargetEntity targetEntity, String ownerUserId, boolean deleteOperation) {
        String currentUserId = securityUtils.currentUserId()
            .orElseThrow(() -> new AccessDeniedException("Authentication is required for this operation"));

        if (deleteOperation && securityUtils.hasRole("ROLE_ADMIN")) {
            return;
        }

        if (ownerUserId == null || ownerUserId.isBlank() || !ownerUserId.equals(currentUserId)) {
            throw new AccessDeniedException("You do not own this " + targetEntity.name().toLowerCase() + " resource");
        }
    }
}
