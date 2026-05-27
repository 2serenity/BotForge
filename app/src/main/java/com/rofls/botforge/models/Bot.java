package com.rofls.botforge.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Bot {
    private String id;
    private String name;
    private String token;
    private String username;
    private BotStatus status;
    private BotMode mode;
    private String templateId;
    private String scriptId;
    private long createdAt;
    private long lastStartedAt;
    private long lastUpdateId;

    public Bot() {
        status = BotStatus.STOPPED;
        mode = BotMode.TEMPLATE;
        createdAt = System.currentTimeMillis();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("token", token);
        json.put("username", username);
        json.put("status", status.name());
        json.put("mode", mode.name());
        json.put("templateId", templateId);
        json.put("scriptId", scriptId);
        json.put("createdAt", createdAt);
        json.put("lastStartedAt", lastStartedAt);
        json.put("lastUpdateId", lastUpdateId);
        return json;
    }

    public static Bot fromJson(JSONObject json) {
        Bot bot = new Bot();
        if (json == null) {
            return bot;
        }
        bot.id = json.optString("id", "");
        bot.name = json.optString("name", "");
        bot.token = json.optString("token", "");
        bot.username = json.optString("username", "");
        bot.status = BotStatus.fromString(json.optString("status", BotStatus.STOPPED.name()));
        bot.mode = BotMode.fromString(json.optString("mode", BotMode.TEMPLATE.name()));
        bot.templateId = json.optString("templateId", "");
        bot.scriptId = json.optString("scriptId", "");
        bot.createdAt = json.optLong("createdAt", System.currentTimeMillis());
        bot.lastStartedAt = json.optLong("lastStartedAt", 0L);
        bot.lastUpdateId = json.optLong("lastUpdateId", 0L);
        return bot;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BotStatus getStatus() {
        return status;
    }

    public void setStatus(BotStatus status) {
        this.status = status;
    }

    public BotMode getMode() {
        return mode;
    }

    public void setMode(BotMode mode) {
        this.mode = mode;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastStartedAt() {
        return lastStartedAt;
    }

    public void setLastStartedAt(long lastStartedAt) {
        this.lastStartedAt = lastStartedAt;
    }

    public long getLastUpdateId() {
        return lastUpdateId;
    }

    public void setLastUpdateId(long lastUpdateId) {
        this.lastUpdateId = lastUpdateId;
    }
}
