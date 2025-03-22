package com.ecs160.hw4;

public class WeightedTotalPostsVisitor extends StatsVisitor {
    private int totalPosts = 0;
    private int longestPostWordCount;

    public WeightedTotalPostsVisitor(int longestPostWordCount) {
        if(longestPostWordCount != 0) {
            this.longestPostWordCount = longestPostWordCount;
        }
        else {
            this.longestPostWordCount = 1;
        }
    }

    @Override
    public void visit(Post post) {
        totalPosts += (1 + (post.getWordCount()) /   (longestPostWordCount));
    }

    @Override
    public double getResult() {
        return totalPosts;
    }
}
