package com.rofls.botforge.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class JsonUtils {
    private JsonUtils() {
    }

    public static JSONArray safeArray(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new JSONArray();
        }
        try {
            return new JSONArray(raw);
        } catch (JSONException ex) {
            return new JSONArray();
        }
    }

    public static JSONObject safeObject(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(raw);
        } catch (JSONException ex) {
            return new JSONObject();
        }
    }
}
