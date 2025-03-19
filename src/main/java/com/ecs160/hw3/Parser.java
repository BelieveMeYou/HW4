package com.ecs160.hw3;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

//Parser class responsible for parsing through the json data of posts and replies of the post
public class Parser {
    public List<Post> parse(String filePath) throws Exception {
        List<Post> posts = new ArrayList<>();
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray feed = root.getAsJsonArray("feed");

            for (JsonElement item : feed) {
                JsonObject thread = item.getAsJsonObject().getAsJsonObject("thread");
                if (thread != null && thread.has("post")) {
                    JsonObject threadPost = thread.getAsJsonObject("post");
                    Post post = parsePost(threadPost);

                    if (thread.has("replies") && thread.get("replies").isJsonArray()) {
                        JsonArray repliesArray = thread.getAsJsonArray("replies");
                        for (JsonElement replyElement : repliesArray) {
                            JsonObject replyJson = replyElement.getAsJsonObject().getAsJsonObject("post");
                            Post reply = parsePost(replyJson);
                            post.addReply(reply);
                        }
                    }
                    posts.add(post);
                }
            }
        }
        return posts;
    }

    private Post parsePost(JsonObject postJson) {
        Post post = new Post();
        post.setContent(postJson.getAsJsonObject("record").get("text").getAsString());

        if (postJson.has("postId")) {
            postIdFieldSetter(post, postJson.get("postId").getAsInt());
        }

        if (postJson.has("likeCount")) {
            post.setLikeCount(postJson.get("likeCount").getAsInt());
        } else {
            post.setLikeCount(0);
        }

        if (postJson.has("replies")) {
            JsonArray repliesArray = postJson.getAsJsonArray("replies");
            for (JsonElement replyElement : repliesArray) {
                JsonObject replyJson = replyElement.getAsJsonObject();
                Post reply = parsePost(replyJson);
                post.addReply(reply);
            }
        }
        return post;
    }


    private void postIdFieldSetter(Post post, Integer id) {
        try {
            var field = Post.class.getDeclaredField("postId");
            field.setAccessible(true);
            field.set(post, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set postId via reflection", e);
        }
    }
}
