package com.pesocial.service;

import com.pesocial.security.TargetEntity;

public interface OwnershipService {
    void assertOwnership(TargetEntity targetEntity, String ownerUserId, boolean deleteOperation);
}
