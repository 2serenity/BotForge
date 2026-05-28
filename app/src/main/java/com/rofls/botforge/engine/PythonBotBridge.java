package com.rofls.botforge.engine;

import com.rofls.botforge.models.Bot;
import com.rofls.botforge.storage.StateManager;
import com.rofls.botforge.storage.StorageManager;
import com.rofls.botforge.telegram.TelegramMessage;
import com.rofls.botforge.utils.JsonUtils;

import org.json.JSONObject;

public class PythonBotBridge {
    private final Bot bot;
    private final TelegramMessage message;
    private final StateManager stateManager;
    private final StorageManager storageManager;

    public PythonBotBridge(
            Bot bot,
            TelegramMessage message,
            StateManager stateManager,
            StorageManager storageManager
    ) {
        this.bot = bot;
        this.message = message;
        this.stateManager = stateManager;
        this.storageManager = storageManager;
    }

    public String getState() {
        return stateManager.getState(bot.getId(), message.getChatId());
    }

    public void setState(String state) {
        stateManager.setState(bot.getId(), message.getChatId(), state);
    }

    public void clearState() {
        stateManager.clearState(bot.getId(), message.getChatId());
    }

    public String getSessionJson() {
        return stateManager.getSession(bot.getId(), message.getChatId()).toString();
    }

    public void setSessionJson(String sessionJson) {
        JSONObject session = JsonUtils.safeObject(sessionJson);
        stateManager.setSession(bot.getId(), message.getChatId(), session);
    }

    public void clearSession() {
        stateManager.clearSession(bot.getId(), message.getChatId());
    }

    public String storageAll(String collection) {
        return storageManager.all(bot.getId(), collection).toString();
    }

    public void storageAdd(String collection, String itemJson) {
        JSONObject item = JsonUtils.safeObject(itemJson);
        storageManager.add(bot.getId(), collection, item);
    }

    public void storageClear(String collection) {
        storageManager.clear(bot.getId(), collection);
    }
}
