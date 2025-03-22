package com.ecs160.hw4;

public class WeightedRepliesVisitor extends StatsVisitor {
    private int totalPosts = 0;
    private double weightedReplies = 0;
    private final int longestPostWordCount;

    public WeightedRepliesVisitor(int longestPostWordCount) {
        this.longestPostWordCount = Math.max(1, longestPostWordCount); // Avoid division by zero
    }

    @Override
    public void visit(Post post) {
        totalPosts++;
        
        for (Post reply : post.getReplies()) {
            weightedReplies += (1 + (reply.getWordCount() / (longestPostWordCount)));
        }
    }

    @Override
    public double getResult() {
        return totalPosts > 0 ? weightedReplies / totalPosts : 0;
    }
}