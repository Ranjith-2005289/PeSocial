package com.pesocial.model.guest;

import java.time.Instant;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Guest {

    private String sessionId;
    private Instant accessStartTime = Instant.now();
    private int accessDurationMinutes = 60;

    public boolean viewPublicPosts() {
        return true;
    }

    public boolean viewPublicProfiles() {
        return true;
    }

    public boolean searchPublicContent() {
        return true;
    }

    public boolean register() {
        return true;
    }

    public boolean login() {
        return true;
    }
}
