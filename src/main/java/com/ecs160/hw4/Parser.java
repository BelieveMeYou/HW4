package com.ecs160.hw4;

import com.google.gson.*;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Parser class responsible for parsing through the json data of posts and extracts them into threads and replies
public class Parser {
    //parses the json file and extracts them into posts as a PostComposite object
    public List<PostComposite> parse(String filePath) throws Exception {
        List<PostComposite> posts = new ArrayList<>();
        Map<String, PostComposite> postMap = new HashMap<>(); // Map to track posts by URI
        
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray feed = root.getAsJsonArray("feed");
            
            // First pass: Create all posts
            for (JsonElement item : feed) {
                if (item.isJsonObject() && item.getAsJsonObject().has("thread")) {
                    JsonObject threadObj = item.getAsJsonObject().getAsJsonObject("thread");
                    processThreadRecursively(threadObj, posts, postMap, null);
                }
            }
            
            // Log statistics for debugging
            int replyCount = 0;
            for (PostComposite post : posts) {
                replyCount += post.getReplies().size();
            }
        }
        return posts;
    }
    
    private void processThreadRecursively(JsonObject threadObj, List<PostComposite> posts, 
                                         Map<String, PostComposite> postMap, PostComposite parent) {
        if (!threadObj.has("post") || threadObj.get("post").isJsonNull()) {
            return;
        }
        
        JsonObject postObj = threadObj.getAsJsonObject("post");
        PostComposite post = new Gson().fromJson(postObj, PostComposite.class);
        
        if (post == null) {
            return;
        }
        
        // Generate a unique ID for the post if it doesn't have one
        Long postId = (long) (posts.size() + 1);
        post.setId(postId);
        post.readPost();
        
        // Store post in collection and map
        posts.add(post);
        
        if (postObj.has("uri") && !postObj.get("uri").isJsonNull()) {
            String uri = postObj.get("uri").getAsString();
            postMap.put(uri, post);
        }
        
        // If this post has a parent, establish the relationship
        if (parent != null) {
            post.setParentId(parent.getId());
            parent.addReply(post);
        }
        
        // Process all replies
        if (threadObj.has("replies") && threadObj.get("replies").isJsonArray()) {
            JsonArray replies = threadObj.getAsJsonArray("replies");
            for (JsonElement replyElement : replies) {
                if (replyElement.isJsonObject()) {
                    processThreadRecursively(
                        replyElement.getAsJsonObject(), 
                        posts, 
                        postMap, 
                        post  // Current post is the parent of its replies
                    );
                }
            }
        }
    }
}
