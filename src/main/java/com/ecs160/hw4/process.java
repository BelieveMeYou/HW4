package com.ecs160.hw4;

import com.google.gson.Gson;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
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
    private static final HashtagService hashtagService = new HashtagService();

    public static void main(String[] args) throws Exception {
        Parser parser = new Parser();
        String filePath = "/Users/kylecvo/ECS-160-HW33/src/main/resources/input.json";
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        List<PostComposite> posts = parser.parse(filePath);

        posts.stream()
                .sorted(Comparator.comparingInt(PostComposite::getLikeCount).reversed())
                .limit(10)
                .forEach(post -> processPost(post));
                
        // Print all hashtags
        hashtagService.printAllPostHashtags(posts);
    }

    private static void processPost(Post post) {
        try {
            System.out.println("Processing Post ID: " + post.getId() + " (Likes: " + 
                    ((PostComposite)post).getLikeCount() + ")");

            // Send to moderation service
            String moderationResult = sendModerationRequest(post);

            if ("FAILED".equals(moderationResult)) {
                System.out.println("[DELETED]");
            } else {
                // Generate hashtag with LLAMA-3
                String hashtag = generateHashtagWithLlama(post);
                hashtagService.setHashtag(post, hashtag);
                System.out.println(post.getContent() + " " + hashtag);
            }

            // Process replies
            List<Post> replies = post.getReplies();
            if (replies.size() >= 2) {
                for (int i = 0; i < 2; i++) {
                    Post reply = replies.get(i);
                    String replyModerationResult = sendModerationRequest(reply);
                    if ("FAILED".equals(replyModerationResult)) {
                        System.out.println("--> [DELETED]");
                    } else {
                        String replyHashtag = generateHashtagWithLlama(reply);
                        hashtagService.setHashtag(reply, replyHashtag);
                        System.out.println("--> " + reply.getContent() + " " + replyHashtag);
                    }
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

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            System.out.println("Warning: Moderation service not available, skipping moderation check");
            return "PASSED"; // Default to passing if service not available
        }
    }

    private static String generateHashtagWithLlama(Post post) {
        try {
            URL url = new URL("http://localhost:11434/api/generate");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Prepare the prompt for LLAMA-3
            String prompt = "Generate a single hashtag (in the format #word) for this social media post: \"" + 
                            post.getContent() + "\"";
            
            String jsonRequest = "{\"model\": \"llama3\", \"prompt\": \"" + 
                                escape(prompt) + 
                                "\", \"stream\": false}";

            // Send the request
            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
                writer.write(jsonRequest);
                writer.flush();
            }

            // Read the response
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse the response
            Map<String, Object> jsonResponse = gson.fromJson(response.toString(), Map.class);
            String generatedText = (String) jsonResponse.get("response");
            
            // Extract the hashtag (simple extraction, might need to be improved)
            String hashtag = extractHashtag(generatedText);
            return hashtag;
            
        } catch (IOException e) {
            System.out.println("Warning: Failed to generate hashtag with LLAMA-3: " + e.getMessage());
            return "#default";
        }
    }
    
    private static String extractHashtag(String text) {
        // Extract a hashtag from the text
        if (text.contains("#")) {
            int hashtagStart = text.indexOf("#");
            int hashtagEnd = text.indexOf(" ", hashtagStart);
            if (hashtagEnd == -1) {
                hashtagEnd = text.length();
            }
            return text.substring(hashtagStart, hashtagEnd).trim();
        }
        return "#default";
    }
    
    private static String escape(String input) {
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }
}