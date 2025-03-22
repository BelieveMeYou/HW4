package com.ecs160.hw4;

public class UnweightedTotalPostsVisitor extends StatsVisitor {
    private int totalPosts = 0;

    @Override
    public void visit(Post post) {
        totalPosts++;
    }

    @Override
    public double getResult() {
        return totalPosts;
    }
}
