package com.pesocial.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pesocial.model.post.Post;
import com.pesocial.model.user.User;
import com.pesocial.service.GuestService;

@RestController
@RequestMapping("/api/guest")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @GetMapping("/posts/public")
    public ResponseEntity<List<Post>> publicPosts() {
        return ResponseEntity.ok(guestService.viewPublicPosts());
    }

    @GetMapping("/profiles/public")
    public ResponseEntity<List<User>> publicProfiles() {
        return ResponseEntity.ok(guestService.viewPublicProfiles());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Post>> search(@RequestParam String query) {
        return ResponseEntity.ok(guestService.searchPublicContent(query));
    }
}
