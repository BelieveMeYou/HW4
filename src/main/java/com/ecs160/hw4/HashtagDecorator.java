package com.ecs160.hw4;

import java.util.List;

public class HashtagDecorator implements Post {
    private final Post decoratedPost;
    private String hashtag;

    public HashtagDecorator(Post post) {
        this.decoratedPost = post;
        this.hashtag = "";
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public String getHashtag() {
        return hashtag;
    }

    // Delegate all Post interface methods to the decorated post
    @Override
    public String getContent() {
        return decoratedPost.getContent();
    }

    @Override
    public void setContent(String content) {
        decoratedPost.setContent(content);
    }

    @Override
    public String getCreatedAt() {
        return decoratedPost.getCreatedAt();
    }

    @Override
    public Long getId() {
        return decoratedPost.getId();
    }

    @Override
    public Long getParentId() {
        return decoratedPost.getParentId();
    }

    @Override
    public void setParentId(Long parentId) {
        decoratedPost.setParentId(parentId);
    }

    @Override
    public int getWordCount() {
        return decoratedPost.getWordCount();
    }

    @Override
    public void readPost() {
        decoratedPost.readPost();
    }

    @Override
    public List<Post> getReplies() {
        return decoratedPost.getReplies();
    }

    @Override
    public void addReply(Post reply) {
        decoratedPost.addReply(reply);
    }
} 