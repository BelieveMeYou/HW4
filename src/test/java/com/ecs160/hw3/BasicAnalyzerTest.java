package com.ecs160.hw1;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicAnalyzerTest {
    private Redis redis;
    private Analyzer analyzer;
    List<PostComposite> posts;

    @BeforeEach
    void setUp() {
        redis = new Redis();
        redis.getJedis().select(1);
        redis.flushDB();
        redis.getJedis().set("post:id", "0");
        posts = new ArrayList<>();
        analyzer = new Analyzer(posts, 0);
    }

    // reset after each test
    @AfterEach
    void reset() {
        redis.close();
    }

    @Test
    void testEmptyDatabase() {
        // make sure all values are 0
        assertEquals(0.0, analyzer.getTotalPosts());
        assertEquals(0.0, analyzer.getAvgReplies());
        assertEquals("00:00:00", analyzer.getAvgInterval());
    }

    @Test
    void testSinglePost() {
        // make and store post
        PostComposite post = createPost("Post", Instant.parse("2024-01-01T00:00:00.000Z"));
        redis.storePost(post, 0L);
        posts.add(post); // Add to list

        // check values
        assertEquals(1.0, analyzer.getTotalPosts());
        assertEquals(0.0, analyzer.getAvgReplies());
    }

    @Test
    void testReplies() {
        // store parent
        PostComposite parent = createPost("Parent post", Instant.now());
        Long parentId = redis.storePost(parent, 0L);

        // store replies
        PostComposite reply1 = createPost("First reply", Instant.now().plusSeconds(60));
        PostComposite reply2 = createPost("Second reply", Instant.now().plusSeconds(120));
        redis.storePost(reply1, parentId);
        redis.storePost(reply2, parentId);

        posts.add(parent);
        posts.add(reply1);
        posts.add(reply2);

        parent.addReply(reply1);
        parent.addReply(reply2);


        // check
        assertEquals(3.0, analyzer.getTotalPosts()); // 3 posts
        assertEquals(2.0/3, analyzer.getAvgReplies()); // 2 out of 3 posts are replies, 2/3
    }

    @Test
    void testAverageInterval() {
        // fake posts with manual timestamps
        PostComposite p1 = createPost("Post 1", Instant.parse("2024-01-01T00:00:00.000Z"));
        PostComposite p2 = createPost("Post 2", Instant.parse("2024-01-01T00:01:00.000Z"));
        PostComposite p3 = createPost("Post 3", Instant.parse("2024-01-01T00:03:00.000Z"));

        // store posts
        redis.storePost(p1, null);
        redis.storePost(p2, null);
        redis.storePost(p3, null);

        posts = Arrays.asList(p1, p2, p3);

        analyzer = new Analyzer(posts, 0);

        // check intervals: 60s and 120s = avg 90s = 00:01:30
        assertEquals("00:01:30", analyzer.getAvgInterval());
    }

    private PostComposite createPost(String content, Instant timestamp) {
        PostComposite post = new PostComposite();
        post.setContent(content);
        if (timestamp != null) {
            post.setCreatedAt(timestamp.toString());
        }
        post.readPost();
        return post;
    }
}
