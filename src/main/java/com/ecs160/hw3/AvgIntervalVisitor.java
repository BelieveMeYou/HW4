package com.ecs160.hw1;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class AvgIntervalVisitor extends StatsVisitor {
    private final List<Post> orderedPosts = new ArrayList<>();

    @Override
    public void visit(Post post) {
        orderedPosts.add(post);
    }

    @Override
    public double getResult() {
        // Sort posts by timestamp before calculating intervals, handling null values
        orderedPosts.sort(Comparator.comparing(
            post -> post.getCreatedAt(), 
            Comparator.nullsLast(String::compareTo)
        ));
        
        // Remove posts with null timestamps after sorting
        orderedPosts.removeIf(post -> post.getCreatedAt() == null);
        
        long totalSeconds = 0;
        if (orderedPosts.size() < 2) return 0;

        for (int i = 1; i < orderedPosts.size(); i++) {
            PostComposite current = (PostComposite) orderedPosts.get(i);
            PostComposite previous = (PostComposite) orderedPosts.get(i-1);
            totalSeconds += current.timePost(previous);
        }

        return totalSeconds / (orderedPosts.size() - 1);
    }
    
    public String getFormattedResult() {
        long totalSeconds = (long) getResult();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}