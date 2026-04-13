package com.pesocial.service.feed;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class FeedPublisher {

    private final List<FollowerObserver> observers = new ArrayList<>();

    public void subscribe(FollowerObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(FollowerObserver observer) {
        observers.remove(observer);
    }

    public void publishPost(String authorId, String postId) {
        for (FollowerObserver observer : observers) {
            observer.onPostPublished(authorId, postId);
        }
    }
}
