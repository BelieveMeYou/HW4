package com.ecs160.hw3;

import com.google.gson.Gson;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class process {

    private static final Gson gson = new Gson();
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public static void main(String[] args) throws Exception {
        Parser parser = new Parser();
        String filePath = "/Users/kylecvo/ECS-160-HW33/src/main/resources/input.json";
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        List<Post> posts = parser.parse(filePath);

        posts.stream()
                .sorted(Comparator.comparingInt(Post::getLikeCount).reversed())
                .limit(10)
                .forEach(post -> processPost(post));
    }

    private static void processPost(Post post) {
        try {
            System.out.println("Processing Post ID: " + post.getId() + " (Likes: " + post.getLikeCount() + ")");

            // Send to moderation service
            String moderationResult = sendModerationRequest(post);

            if ("FAILED".equals(moderationResult)) {
                System.out.println("[DELETED]");
            } else {
                // Send to hashtagging service
                String hashtag = sendHashtagRequest(post);
                System.out.println(post.getContent() + " " + hashtag);
            }

            for (int i = 0; i < 2; i++) {
                Post reply = post.getReplies().get(i);
                String replyModerationResult = sendModerationRequest(reply);
                if ("FAILED".equals(replyModerationResult)) {
                    System.out.println("--> [DELETED]");
                } else {
                    String replyHashtag = sendHashtagRequest(reply);
                    System.out.println("--> " + reply.getContent() + " " + replyHashtag);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String sendModerationRequest(Post post) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:30001/moderate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(post.getContent()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private static String sendHashtagRequest(Post post) throws Exception {
        String requestBody = gson.toJson(Map.of("postContent", post.getContent()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:30002/api/hashtag"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}