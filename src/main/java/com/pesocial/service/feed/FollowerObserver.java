package com.pesocial.service.feed;

public interface FollowerObserver {
    void onPostPublished(String authorId, String postId);
}
