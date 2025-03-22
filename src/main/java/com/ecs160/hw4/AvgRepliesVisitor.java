package com.ecs160.hw4;

public class AvgRepliesVisitor extends StatsVisitor {
    private int totalReplies = 0;
    private int totalPosts = 0;

    @Override
    public void visit(Post post) {
        totalPosts++;
        totalReplies += post.getReplies().size();
    }

    @Override
    public double getResult() {
        if(totalPosts > 0) {
            return  (double) (totalReplies / totalPosts);
        }
        else {
            return 0;
        }
    }
}