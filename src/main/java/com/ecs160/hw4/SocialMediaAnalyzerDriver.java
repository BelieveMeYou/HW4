package com.ecs160.hw4;

import org.apache.commons.cli.*;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
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
import java.util.Map;

public class SocialMediaAnalyzerDriver {
    private static final Gson gson = new Gson();
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private static HashtagDecorator processPost(Post post) {
        HashtagDecorator decoratedPost = new HashtagDecorator(post);
        try {
            String hashtag = sendHashtagRequest(post);
            decoratedPost.setHashtag(hashtag);

            List<Post> replies = post.getReplies();
            if (replies.size() >= 2) {
                for (int i = 0; i < 2; i++) {
                    Post reply = replies.get(i);
                    String replyHashtag = sendHashtagRequest(reply);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decoratedPost;
    }


    public static String sendHashtagRequest(Post post) throws Exception {
        String content = (post.getContent() != null) ? post.getContent() : "[Empty Content]";

        String requestBody = gson.toJson(Map.of("postContent", post.getContent()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:30002/api/hashtag"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static void printHashtags(List<HashtagDecorator> decoratedPosts) {
        System.out.println("\n=== Hashtag Results ===");
        for (HashtagDecorator post : decoratedPosts) {
            System.out.println("Post ID: " + post.getId() + " - Hashtag: " + (post.getHashtag().isEmpty() ? "[No Hashtag]" : post.getHashtag()));
        }
    }

    public static void main(String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();
            Options options = new Options();
            options.addOption("weighted", false, "use weighted analysis");
            options.addOption("file", true, "path to JSON file");
            CommandLine cmd = parser.parse(options, args);

            Config config = Config.getInstance();
            config.setWeighted(cmd.hasOption("weighted"));
            config.setFilePath(cmd.getOptionValue("file", "src/main/resources/input.json"));
            String filePath = cmd.getOptionValue("file", "src/main/resources/input.json");

            File file = new File(config.getFilePath());
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            Parser jsonParser = new Parser();

            scanner.close();

            Redis redis = new Redis();

            List<PostComposite> posts = jsonParser.parse(config.getFilePath());
            List<PostComposite> poster = jsonParser.parse(filePath);

            List<HashtagDecorator> decoratedPosts = poster.stream()
                    .sorted(Comparator.comparingInt(PostComposite::getLikeCount).reversed())
                    .limit(10)
                    .map(SocialMediaAnalyzerDriver::processPost)
                    .toList();

            printHashtags(decoratedPosts);

            for (PostComposite post : posts) {
                redis.storePost(post, post.getParentId());
            }

            int longestWordCount = redis.getLongestPostWordCount();

            if (config.isWeighted()) {
                WeightedTotalPostsVisitor totalVisitor = new WeightedTotalPostsVisitor(longestWordCount);
                for (Post post : posts) {
                    totalVisitor.visit(post);
                }
                System.out.println("Weighted Total Posts: " + totalVisitor.getResult());

                WeightedRepliesVisitor repliesVisitor = new WeightedRepliesVisitor(longestWordCount);
                for (Post post : posts) {
                    repliesVisitor.visit(post);
                }
                System.out.println("Weighted Average Replies: " + repliesVisitor.getResult());
            } else {
                UnweightedTotalPostsVisitor totalVisitor = new UnweightedTotalPostsVisitor();
                for (Post post : posts) {
                    totalVisitor.visit(post);
                }
                System.out.println("Total Posts: " + totalVisitor.getResult());

                AvgRepliesVisitor repliesVisitor = new AvgRepliesVisitor();
                for (Post post : posts) {
                    repliesVisitor.visit(post);
                }
                System.out.println("Average Replies: " + repliesVisitor.getResult());
            }

            AvgIntervalVisitor intervalVisitor = new AvgIntervalVisitor();
            for (Post post : posts) {
                intervalVisitor.visit(post);
            }
            System.out.println("Average Interval: " + intervalVisitor.getFormattedResult());


            redis.close();

        } catch (FileNotFoundException e) {
            System.err.println("Error: Input file not found - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}