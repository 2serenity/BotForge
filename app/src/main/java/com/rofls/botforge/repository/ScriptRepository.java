package com.rofls.botforge.repository;

import android.content.Context;
import android.content.SharedPreferences;

public class ScriptRepository {
    private static final String PREFS_NAME = "botforge_scripts";
    private static final String KEY_PREFIX = "script_";

    private final SharedPreferences prefs;

    public ScriptRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getScript(String scriptId) {
        if (scriptId == null) {
            return "";
        }
        return prefs.getString(KEY_PREFIX + scriptId, "");
    }

    public void saveScript(String scriptId, String script) {
        if (scriptId == null || scriptId.trim().isEmpty()) {
            return;
        }
        prefs.edit().putString(KEY_PREFIX + scriptId, script == null ? "" : script).apply();
    }

    public void deleteScript(String scriptId) {
        if (scriptId == null || scriptId.trim().isEmpty()) {
            return;
        }
        prefs.edit().remove(KEY_PREFIX + scriptId).apply();
    }
}
