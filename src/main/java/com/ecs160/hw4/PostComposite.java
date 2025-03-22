package com.ecs160.hw4;

import com.google.gson.annotations.SerializedName;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


// Implementation for both single posts and thread posts
public class PostComposite implements Post {  
    @SerializedName("record")
    private Record record;
    
    private Long id;
    private Long parentId;
    private int postLength;
    private int wordCount;
    private int likeCount;
    private transient Instant timestamp;
    private final List<Post> replies;
    
    public static class Record {
        @SerializedName("$type")
        private String type;
        
        @SerializedName("text")
        private String text;
        
        @SerializedName("createdAt")
        private String createdAt;
    }

    public PostComposite() {
        this.replies = new ArrayList<>();
        this.record = new Record();
        this.likeCount = 0;
    }

    public String getContent() {
        return record != null ? record.text : null;
    }

    public void setContent(String content) {
        if (record == null) {
            record = new Record();
        }
        record.text = content;
    }

    @Override
    public String getCreatedAt() {
        return record != null ? record.createdAt : null;
    }

    public void setCreatedAt(String createdAt) {
        if (record == null) {
            record = new Record();
        }
        record.createdAt = createdAt;
    }

    @Override
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public int getWordCount() {
        return wordCount;
    }

    @Override
    public void readPost() {
        String content = getContent();
        if (content != null) {
            this.postLength = content.length();
            this.wordCount = content.split("\\s+").length;
        } else {
            this.postLength = 0;
            this.wordCount = 0;
        }

        String createdAt = getCreatedAt();
        if (createdAt != null) {
            try {
                this.timestamp = Instant.parse(createdAt);
            } catch (Exception e) {
                this.timestamp = Instant.parse("2024-01-01T00:00:00Z");
            }
        } else {
            this.timestamp = Instant.parse("2024-01-01T00:00:00Z");
        }
    }

    @Override
    public List<Post> getReplies() {
        return replies;
    }

    @Override
    public void addReply(Post reply) {
        replies.add(reply);
        reply.setParentId(this.getId());
    }

    public long timePost(Post previousPost) {
        if (previousPost == null || previousPost.getCreatedAt() == null || this.getCreatedAt() == null) {
            System.out.println("Skipping invalid timestamp pair");
            return 0;
        }

        try {
            // Try parsing with multiple formats to handle different JSON date formats
            Instant previousInstant = parseTimestamp(previousPost.getCreatedAt());
            Instant currentInstant = parseTimestamp(this.getCreatedAt());
            
            if (previousInstant == null || currentInstant == null) {
                System.out.println("Failed to parse timestamps");
                return 0;
            }

            long seconds = ChronoUnit.SECONDS.between(previousInstant, currentInstant);
            if (seconds < 0) {
                // Make sure we don't return negative durations
                seconds = -seconds;
            }
            return seconds;
        } catch (Exception e) {
            System.out.println("Error calculating time interval: " + e.getMessage());
            return 0;
        }
    }

    private Instant parseTimestamp(String timestamp) {
        try {
            return Instant.parse(timestamp);
        } catch (Exception e1) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .withZone(java.time.ZoneId.of("UTC"));
                return ZonedDateTime.parse(timestamp, formatter).toInstant();
            } catch (Exception e2) {
                try {
                    // Try with another possible format
                    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                            .withZone(java.time.ZoneId.of("UTC"));
                    return ZonedDateTime.parse(timestamp, formatter2).toInstant();
                } catch (Exception e3) {
                    System.out.println("Failed to parse timestamp: " + timestamp);
                    return null;
                }
            }
        }
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}