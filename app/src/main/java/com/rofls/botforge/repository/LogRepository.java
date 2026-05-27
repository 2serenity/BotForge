package com.rofls.botforge.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.LogEntry;
import com.rofls.botforge.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class LogRepository {
    private static final String PREFS_NAME = "botforge_logs";
    private static final String KEY_LOGS = "logs";
    private static final int MAX_LOGS = 300;

    private final SharedPreferences prefs;

    public LogRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public synchronized void info(Bot bot, String message) {
        add(bot, "INFO", message, "");
    }

    public synchronized void warn(Bot bot, String message, String details) {
        add(bot, "WARN", message, details);
    }

    public synchronized void error(Bot bot, String message, String details) {
        add(bot, "ERROR", message, details);
    }

    public synchronized void add(Bot bot, String level, String message, String details) {
        LogEntry entry = new LogEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setBotId(bot == null ? "" : bot.getId());
        entry.setBotName(bot == null ? "BotForge" : bot.getName());
        entry.setLevel(level);
        entry.setMessage(message);
        entry.setDetails(details == null ? "" : details);
        entry.setTimestamp(System.currentTimeMillis());

        List<LogEntry> logs = getAllLogs();
        logs.add(0, entry);
        if (logs.size() > MAX_LOGS) {
            logs = logs.subList(0, MAX_LOGS);
        }
        writeLogs(logs);
    }

    public synchronized List<LogEntry> getAllLogs() {
        JSONArray array = JsonUtils.safeArray(prefs.getString(KEY_LOGS, "[]"));
        List<LogEntry> logs = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            logs.add(LogEntry.fromJson(array.optJSONObject(i)));
        }
        Collections.sort(logs, new Comparator<LogEntry>() {
            @Override
            public int compare(LogEntry left, LogEntry right) {
                return Long.compare(right.getTimestamp(), left.getTimestamp());
            }
        });
        return logs;
    }

    public synchronized List<LogEntry> getLogsForBot(String botId) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry entry : getAllLogs()) {
            if (botId != null && botId.equals(entry.getBotId())) {
                result.add(entry);
            }
        }
        return result;
    }

    public synchronized void clearAll() {
        prefs.edit().remove(KEY_LOGS).apply();
    }

    public synchronized void clearForBot(String botId) {
        if (botId == null) {
            return;
        }
        List<LogEntry> remaining = new ArrayList<>();
        for (LogEntry entry : getAllLogs()) {
            if (!botId.equals(entry.getBotId())) {
                remaining.add(entry);
            }
        }
        writeLogs(remaining);
    }

    private void writeLogs(List<LogEntry> logs) {
        JSONArray array = new JSONArray();
        for (LogEntry entry : logs) {
            try {
                array.put(entry.toJson());
            } catch (JSONException ignored) {
                // Ignore a broken log entry; logs must never break the app.
            }
        }
        prefs.edit().putString(KEY_LOGS, array.toString()).apply();
    }
}
