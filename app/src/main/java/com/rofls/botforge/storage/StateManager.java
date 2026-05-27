package com.rofls.botforge.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.rofls.botforge.utils.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class StateManager {
    private static final String PREFS_NAME = "botforge_state";

    private final SharedPreferences prefs;

    public StateManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getState(String botId, long chatId) {
        return prefs.getString(stateKey(botId, chatId), "");
    }

    public void setState(String botId, long chatId, String state) {
        prefs.edit().putString(stateKey(botId, chatId), state == null ? "" : state).apply();
    }

    public void clearState(String botId, long chatId) {
        prefs.edit().remove(stateKey(botId, chatId)).apply();
    }

    public JSONObject getSession(String botId, long chatId) {
        return JsonUtils.safeObject(prefs.getString(sessionKey(botId, chatId), "{}"));
    }

    public void setSessionValue(String botId, long chatId, String key, String value) {
        JSONObject session = getSession(botId, chatId);
        try {
            session.put(key, value);
        } catch (JSONException ignored) {
        }
        prefs.edit().putString(sessionKey(botId, chatId), session.toString()).apply();
    }

    public String getSessionValue(String botId, long chatId, String key) {
        return getSession(botId, chatId).optString(key, "");
    }

    public void clearSession(String botId, long chatId) {
        prefs.edit().remove(sessionKey(botId, chatId)).apply();
    }

    private String stateKey(String botId, long chatId) {
        return "bot_" + botId + "_chat_" + chatId + "_state";
    }

    private String sessionKey(String botId, long chatId) {
        return "bot_" + botId + "_chat_" + chatId + "_session";
    }
}
