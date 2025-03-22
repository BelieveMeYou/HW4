package com.ecs160.hw4;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashtagService {
    private final Map<Long, HashtagDecorator> decoratedPosts = new HashMap<>();
    
    public void setHashtag(Post post, String hashtag) {
        Long postId = post.getId();
        HashtagDecorator decorator;
        
        if (decoratedPosts.containsKey(postId)) {
            decorator = decoratedPosts.get(postId);
        } else {
            decorator = new HashtagDecorator(post);
            decoratedPosts.put(postId, decorator);
        }
        
        decorator.setHashtag(hashtag);
    }
    
    public String getHashtag(Post post) {
        Long postId = post.getId();
        if (decoratedPosts.containsKey(postId)) {
            return decoratedPosts.get(postId).getHashtag();
        }
        return "";
    }
    
    public void printHashtags() {
        System.out.println("\n=== Post Hashtags ===");
        for (Map.Entry<Long, HashtagDecorator> entry : decoratedPosts.entrySet()) {
            Post post = entry.getValue();
            String hashtag = entry.getValue().getHashtag();
            System.out.println("Post ID: " + post.getId() + 
                            ", Content: " + post.getContent() + 
                            ", Hashtag: " + (hashtag.isEmpty() ? "[No hashtag]" : hashtag));
        }
    }
    
    public void printAllPostHashtags(List<? extends Post> posts) {
        System.out.println("\n=== All Posts Hashtags ===");
        for (Post post : posts) {
            String hashtag = getHashtag(post);
            System.out.println("Post ID: " + post.getId() + 
                            ", Content: " + post.getContent() + 
                            ", Hashtag: " + (hashtag.isEmpty() ? "[No hashtag]" : hashtag));
        }
    }
} 