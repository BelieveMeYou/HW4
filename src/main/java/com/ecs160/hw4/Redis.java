package com.ecs160.hw4;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;


//using a redis based storage for data efficiency and the retrieval of posts
public class Redis {
    private final Jedis jedis; //redis client instance
    private static final String POST_COUNTER = "post:id"; // key for generating unique post IDs

    //initializes a connection to localhost on port 6379
    public Redis() {
        String redisHost;
        String redisPort;

        if (System.getenv("REDIS_HOST") != null){
            redisHost = System.getenv("REDIS_HOST");
        }
        else{
            redisHost = "localhost";
        }
        if (System.getenv("REDIS_PORT") != null){
            redisPort = System.getenv("REDIS_PORT");
        }
        else{
            redisPort = "6379";
        }
        this.jedis = new Jedis(redisHost, Integer.parseInt(redisPort));
    }

    //stores a new post in Redis and gives it a unique ID
    public Long storePost(Post post, Long parentId) {
        try {
            Long id = jedis.incr(POST_COUNTER);

            String content = post.getContent() != null ? post.getContent() : "";
            String timestamp = post.getCreatedAt() != null ? post.getCreatedAt() : "";

            Transaction transaction = jedis.multi(); //redis transaction
            transaction.hset("post:" + id, "content", content);
            transaction.hset("post:" + id, "wordCount", String.valueOf(post.getWordCount()));
            transaction.hset("post:" + id, "timestamp", timestamp);

            //if the post has a parent then store the relationship
            if (parentId != null) {
                transaction.sadd("post:" + parentId + ":replies", id.toString());
            }
            transaction.exec(); //execute transaction

            return id;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //access to the jedis instance
    public Jedis getJedis() {
        return this.jedis;
    }

    //retrieves the post stored in Redis
    public List<PostComposite> getAllPosts() {
        List<PostComposite> posts = new ArrayList<>();
        Set<String> keys = jedis.keys("post:*");

        for (String key : keys) {
            if (key.matches("post:\\d+")) {
                PostComposite post = new PostComposite();
                post.setContent(jedis.hget(key, "content"));
                post.setCreatedAt(jedis.hget(key, "timestamp"));
                post.readPost();
                posts.add(post);
            }
        }

        return posts;
    }

    //retrieves highest word count among all the stored posts
    public int getLongestPostWordCount() {
        return jedis.keys("post:*").stream()
                .filter(key -> key.matches("post:\\d+"))
                .mapToInt(key -> {
                    String wordCount = jedis.hget(key, "wordCount");
                    return wordCount != null ? Integer.parseInt(wordCount) : 0;
                })
                .max()
                .orElse(0);
    }

    //clear the data stored in the redis database
    public void flushDB() {
        jedis.flushDB();
    }

    //close the connection to redis
    public void close() {
        jedis.close();
    }
}