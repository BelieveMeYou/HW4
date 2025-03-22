package com.ecs160.hw4;

import java.util.List;


public class Analyzer {
    private final int longestPostWordCount; 
    private final List<PostComposite> posts; 


    public Analyzer(List<PostComposite> posts, int longestPostWordCount) {
        this.posts = posts;
        this.longestPostWordCount = longestPostWordCount;
    }

    public double getAvgReplies() {
        if (posts.isEmpty()) return 0;

        long totalReplies = 0;
        for (PostComposite post : posts) {
            totalReplies += post.getReplies().size();
        }

        return totalReplies / (double) posts.size();
    }

    public String getAvgInterval() {
        long totalSeconds = 0;
        if (posts.isEmpty() || posts.size() < 2) return "00:00:00";

        for (int i = 1; i < posts.size(); i++) {
            totalSeconds += posts.get(i).timePost(posts.get(i - 1));
        }

        long averageSeconds = totalSeconds / (posts.size() - 1);
        return formatDuration(averageSeconds);
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public double getTotalPosts() {
        return posts.size();
    }

    public double getWeightedTotalPosts() {
        if (posts.isEmpty()) return 0;

        long totalWeight = 0;
        for (PostComposite post : posts) {
            totalWeight += (1 + (post.getWordCount() / (longestPostWordCount)));
        }
        return totalWeight;
    }

    public double getWeightedAvgReplies() {
        if (posts.isEmpty()) return 0;

        long totalWeight = 0;
        long totalReplies = 0;

        for (PostComposite post : posts) {
            System.out.println("totalReplies: " + post.getReplies().size());
            for (Post reply : post.getReplies()) {
                totalWeight += (1 + (reply.getWordCount() / (longestPostWordCount)));
                totalReplies++;
            }
        }

        System.out.println("totalWeight: " + totalWeight);
        System.out.println("totalReplies: " + totalReplies);
        return(double) (totalWeight / getTotalPosts());
    }
}