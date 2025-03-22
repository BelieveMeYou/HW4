package com.ecs160.hw4;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;


public class SocialMediaAnalyzerDriver {
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

            File file = new File(config.getFilePath());
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
               scanner.nextLine();
            }
            scanner.close();

            Redis redis = new Redis();

            Parser jsonParser = new Parser();
            List<PostComposite> posts = jsonParser.parse(config.getFilePath());

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