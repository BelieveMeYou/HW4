package com.ecs160.hw1;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeightedAnalyzerTest {
    private Redis redis;
    private Analyzer analyzer;
    List<PostComposite> posts;

    // set up test db
    @BeforeEach
    void setUp() {
        redis = new Redis();
        redis.getJedis().select(1);
        redis.flushDB();
        redis.getJedis().set("post:id", "0");
        posts = new ArrayList<>(); // Initialize posts list
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
        setUp();
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

        int longestWordCount = posts.stream()
                .mapToInt(PostComposite::getWordCount)
                .max()
                .orElse(1);

        analyzer = new Analyzer(posts, longestWordCount);
        // check values
        assertEquals(2.0, analyzer.getWeightedTotalPosts());
        assertEquals(0.0, analyzer.getWeightedAvgReplies());
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

        int longestWordCount = posts.stream()
                .mapToInt(PostComposite::getWordCount)
                .max()
                .orElse(1);

        analyzer = new Analyzer(posts, longestWordCount);

        // check
        assertEquals(6.0, analyzer.getWeightedTotalPosts()); // 3 posts, 2 words each: 3(1+(2/2)) = 6
        assertEquals(4.0/3, analyzer.getWeightedAvgReplies()); // 2 replies, 2 words each: 2(1+(2/2)/3 = 4/3
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
