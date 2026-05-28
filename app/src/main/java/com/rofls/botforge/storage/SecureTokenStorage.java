package com.rofls.botforge.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

@SuppressWarnings("deprecation")
public class SecureTokenStorage {
    private static final String PREFS_NAME = "botforge_secure_tokens";
    private static final String KEY_PREFIX = "token_";

    private final SharedPreferences encryptedPrefs;

    public SecureTokenStorage(Context context) {
        encryptedPrefs = createEncryptedPrefs(context.getApplicationContext());
    }

    public String getToken(String botId) {
        if (botId == null || botId.trim().isEmpty()) {
            return "";
        }
        return encryptedPrefs.getString(key(botId), "");
    }

    public void saveToken(String botId, String token) {
        if (botId == null || botId.trim().isEmpty()) {
            return;
        }
        encryptedPrefs.edit()
                .putString(key(botId), token == null ? "" : token)
                .apply();
    }

    public void deleteToken(String botId) {
        if (botId == null || botId.trim().isEmpty()) {
            return;
        }
        encryptedPrefs.edit().remove(key(botId)).apply();
    }

    private String key(String botId) {
        return KEY_PREFIX + botId;
    }

    private SharedPreferences createEncryptedPrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalStateException("Не удалось открыть защищённое хранилище токенов", ex);
        }
    }
}
