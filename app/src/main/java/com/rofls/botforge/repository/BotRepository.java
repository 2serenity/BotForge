package com.rofls.botforge.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotStatus;
import com.rofls.botforge.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class BotRepository {
    private static final String PREFS_NAME = "botforge_bots";
    private static final String KEY_BOTS = "bots";

    private final SharedPreferences prefs;

    public BotRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public synchronized List<Bot> getAllBots() {
        JSONArray array = JsonUtils.safeArray(prefs.getString(KEY_BOTS, "[]"));
        List<Bot> bots = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            bots.add(Bot.fromJson(array.optJSONObject(i)));
        }
        Collections.sort(bots, new Comparator<Bot>() {
            @Override
            public int compare(Bot left, Bot right) {
                return Long.compare(right.getCreatedAt(), left.getCreatedAt());
            }
        });
        return bots;
    }

    public synchronized Bot getBot(String botId) {
        if (botId == null) {
            return null;
        }
        for (Bot bot : getAllBots()) {
            if (botId.equals(bot.getId())) {
                return bot;
            }
        }
        return null;
    }

    public synchronized Bot saveBot(Bot bot) {
        if (bot.getId() == null || bot.getId().trim().isEmpty()) {
            bot.setId(UUID.randomUUID().toString());
        }
        if (bot.getScriptId() == null || bot.getScriptId().trim().isEmpty()) {
            bot.setScriptId(bot.getId());
        }
        if (bot.getCreatedAt() <= 0L) {
            bot.setCreatedAt(System.currentTimeMillis());
        }

        List<Bot> bots = getAllBots();
        boolean replaced = false;
        for (int i = 0; i < bots.size(); i++) {
            if (bot.getId().equals(bots.get(i).getId())) {
                bots.set(i, bot);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            bots.add(bot);
        }
        writeBots(bots);
        return bot;
    }

    public synchronized void deleteBot(String botId) {
        List<Bot> bots = getAllBots();
        List<Bot> filtered = new ArrayList<>();
        for (Bot bot : bots) {
            if (!bot.getId().equals(botId)) {
                filtered.add(bot);
            }
        }
        writeBots(filtered);
    }

    public synchronized void updateStatus(String botId, BotStatus status) {
        Bot bot = getBot(botId);
        if (bot == null) {
            return;
        }
        bot.setStatus(status);
        saveBot(bot);
    }

    public synchronized void markStarted(String botId) {
        Bot bot = getBot(botId);
        if (bot == null) {
            return;
        }
        bot.setStatus(BotStatus.RUNNING);
        bot.setLastStartedAt(System.currentTimeMillis());
        saveBot(bot);
    }

    public synchronized void updateLastUpdateId(String botId, long updateId) {
        Bot bot = getBot(botId);
        if (bot == null) {
            return;
        }
        bot.setLastUpdateId(updateId);
        saveBot(bot);
    }

    private void writeBots(List<Bot> bots) {
        JSONArray array = new JSONArray();
        for (Bot bot : bots) {
            try {
                array.put(bot.toJson());
            } catch (JSONException ignored) {
                // Broken single records are skipped instead of corrupting the whole list.
            }
        }
        prefs.edit().putString(KEY_BOTS, array.toString()).apply();
    }
}
