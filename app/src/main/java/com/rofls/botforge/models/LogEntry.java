package com.rofls.botforge.models;

import org.json.JSONException;
import org.json.JSONObject;

public class LogEntry {
    private String id;
    private String botId;
    private String botName;
    private String level;
    private String message;
    private String details;
    private long timestamp;

    public LogEntry() {
        timestamp = System.currentTimeMillis();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("botId", botId);
        json.put("botName", botName);
        json.put("level", level);
        json.put("message", message);
        json.put("details", details);
        json.put("timestamp", timestamp);
        return json;
    }

    public static LogEntry fromJson(JSONObject json) {
        LogEntry entry = new LogEntry();
        if (json == null) {
            return entry;
        }
        entry.id = json.optString("id", "");
        entry.botId = json.optString("botId", "");
        entry.botName = json.optString("botName", "");
        entry.level = json.optString("level", "INFO");
        entry.message = json.optString("message", "");
        entry.details = json.optString("details", "");
        entry.timestamp = json.optLong("timestamp", System.currentTimeMillis());
        return entry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBotId() {
        return botId;
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
