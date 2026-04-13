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
@TypeAlias("admin")
public class Admin extends User {

    @Field("admin_level")
    private int adminLevel;

    public void suspendUser() {
        // Domain hook for moderation workflow.
    }

    public void banUser() {
        // Domain hook for moderation workflow.
    }

    public void approveCreator() {
        // Domain hook for creator verification workflow.
    }

    public void generateSystemReport() {
        // Domain hook for reporting workflow.
    }

    @Override
    public String getUserType() {
        return "ADMIN";
    }
}
