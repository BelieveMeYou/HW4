package com.ecs160.hw4;

import java.util.List;

// Common interface for all types of posts
public interface Post {
    String getContent();
    void setContent(String content);
    String getCreatedAt();
    Long getId();
    Long getParentId();
    void setParentId(Long parentId);
    int getWordCount();
    void readPost();
    List<Post> getReplies();
    void addReply(Post reply);
}