package com.rofls.botforge.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.rofls.botforge.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class StorageManager {
    private static final String PREFS_NAME = "botforge_storage";

    private final SharedPreferences prefs;

    public StorageManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public JSONArray all(String botId, String collection) {
        return JsonUtils.safeArray(prefs.getString(key(botId, collection), "[]"));
    }

    public void add(String botId, String collection, JSONObject item) {
        JSONArray array = all(botId, collection);
        array.put(item);
        prefs.edit().putString(key(botId, collection), array.toString()).apply();
    }

    public void clear(String botId, String collection) {
        prefs.edit().remove(key(botId, collection)).apply();
    }

    private String key(String botId, String collection) {
        return "bot_" + botId + "_collection_" + collection;
    }
}
