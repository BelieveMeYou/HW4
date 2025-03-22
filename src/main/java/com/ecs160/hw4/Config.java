package com.ecs160.hw4;

public class Config {
    private static Config instance;
    private boolean weighted;
    private String filePath;

    // Private constructor prevents direct instantiation
    private Config() {
        // Default values
        this.weighted = true;
        this.filePath = "src/main/resources/input.json";
    }

    // Lazy initialization with thread safety
    public static synchronized Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    // Getters and setters
    public boolean isWeighted() {
        return weighted;
    }

    public void setWeighted(boolean weighted) {
        this.weighted = weighted;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}