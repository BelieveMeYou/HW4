package com.ecs160.hw3;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private static int nextId = 1;

    private Integer postId; // Unique identifier for posts
    private String postContent; // Text content of post
    private final List<Post> replies; // List of direct replies
    private Integer likeCount;

    public Post() {
        this.replies = new ArrayList<>();
        this.likeCount = 0;
        this.postId = nextId++;
    }

    public Post(Integer postId, String postContent, Integer likeCount) {
        this.postId = postId;
        this.postContent = postContent;
        this.replies = new ArrayList<>();
        this.likeCount = likeCount;
    }

    public Integer getId() {
        return postId;
    }

    public String getContent() {
        return postContent;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public void setContent(String postContent) {
        this.postContent = postContent;
    }

    public List<Post> getReplies() {
        return replies;
    }

    public void addReply(Post reply) {
        this.replies.add(reply);
    }
}
