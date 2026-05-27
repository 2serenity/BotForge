package com.rofls.botforge.models;

public class BotTemplate {
    private final String id;
    private final String name;
    private final String description;
    private final String difficulty;
    private final String defaultScript;

    public BotTemplate(String id, String name, String description, String difficulty, String defaultScript) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.defaultScript = defaultScript;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getDefaultScript() {
        return defaultScript;
    }

    @Override
    public String toString() {
        return name;
    }
}
