package com.rofls.botforge.models;

import org.json.JSONException;
import org.json.JSONObject;

public class BotTemplate {
    private String id;
    private String name;
    private String description;
    private String difficulty;
    private String defaultScript;
    private boolean builtIn;
    private long createdAt;
    private long updatedAt;

    public BotTemplate(String id, String name, String description, String difficulty, String defaultScript) {
        this(id, name, description, difficulty, defaultScript, true, System.currentTimeMillis(), System.currentTimeMillis());
    }

    public BotTemplate(
            String id,
            String name,
            String description,
            String difficulty,
            String defaultScript,
            boolean builtIn,
            long createdAt,
            long updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.defaultScript = defaultScript;
        this.builtIn = builtIn;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("description", description);
        json.put("difficulty", difficulty);
        json.put("defaultScript", defaultScript);
        json.put("builtIn", builtIn);
        json.put("createdAt", createdAt);
        json.put("updatedAt", updatedAt);
        return json;
    }

    public static BotTemplate fromJson(JSONObject json) {
        if (json == null) {
            long now = System.currentTimeMillis();
            return new BotTemplate("", "", "", "", "", false, now, now);
        }
        long now = System.currentTimeMillis();
        return new BotTemplate(
                json.optString("id", ""),
                json.optString("name", ""),
                json.optString("description", ""),
                json.optString("difficulty", "Пользовательский"),
                json.optString("defaultScript", ""),
                json.optBoolean("builtIn", false),
                json.optLong("createdAt", now),
                json.optLong("updatedAt", now)
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDefaultScript() {
        return defaultScript;
    }

    public void setDefaultScript(String defaultScript) {
        this.defaultScript = defaultScript;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return builtIn ? name : name + " (мой)";
    }
}
