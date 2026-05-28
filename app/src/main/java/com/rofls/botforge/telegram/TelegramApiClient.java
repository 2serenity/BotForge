package com.rofls.botforge.telegram;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TelegramApiClient {
    private static final String API_BASE = "https://api.telegram.org/bot";
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 35000;
    public static final int MAX_MESSAGE_LENGTH = 4096;

    public String getMe(String token) throws IOException, TelegramApiException, JSONException {
        JSONObject response = request("GET", token, "getMe", "");
        JSONObject result = response.getJSONObject("result");
        return result.optString("username", "");
    }

    public List<TelegramUpdate> getUpdates(String token, long offset)
            throws IOException, TelegramApiException, JSONException {
        String query = "timeout=25&limit=20";
        if (offset > 0L) {
            query += "&offset=" + offset;
        }
        JSONObject response = request("GET", token, "getUpdates?" + query, "");
        JSONArray result = response.optJSONArray("result");
        List<TelegramUpdate> updates = new ArrayList<>();
        if (result == null) {
            return updates;
        }
        for (int i = 0; i < result.length(); i++) {
            JSONObject updateJson = result.optJSONObject(i);
            if (updateJson == null) {
                continue;
            }
            long updateId = updateJson.optLong("update_id", 0L);
            TelegramMessage message = parseMessage(updateJson.optJSONObject("message"));
            updates.add(new TelegramUpdate(updateId, message));
        }
        return updates;
    }

    public void sendMessage(String token, long chatId, String text)
            throws IOException, TelegramApiException, JSONException {
        sendMessage(token, chatId, text, null);
    }

    public void sendMessage(String token, long chatId, String text, JSONArray buttons)
            throws IOException, TelegramApiException, JSONException {
        List<String> chunks = splitMessageText(text);
        for (int i = 0; i < chunks.size(); i++) {
            JSONArray chunkButtons = i == chunks.size() - 1 ? buttons : null;
            sendSingleMessage(token, chatId, chunks.get(i), chunkButtons);
        }
    }

    public static List<String> splitMessageText(String text) {
        String safeText = text == null ? "" : text;
        List<String> chunks = new ArrayList<>();
        if (safeText.length() <= MAX_MESSAGE_LENGTH) {
            chunks.add(safeText);
            return chunks;
        }

        int start = 0;
        while (start < safeText.length()) {
            int end = Math.min(start + MAX_MESSAGE_LENGTH, safeText.length());
            if (end < safeText.length() && Character.isHighSurrogate(safeText.charAt(end - 1))) {
                end--;
            }
            chunks.add(safeText.substring(start, end));
            start = end;
        }
        return chunks;
    }

    private void sendSingleMessage(String token, long chatId, String text, JSONArray buttons)
            throws IOException, TelegramApiException, JSONException {
        StringBuilder body = new StringBuilder();
        appendForm(body, "chat_id", String.valueOf(chatId));
        appendForm(body, "text", text);
        if (buttons != null && buttons.length() > 0) {
            JSONObject markup = new JSONObject();
            markup.put("keyboard", buttons);
            markup.put("resize_keyboard", true);
            appendForm(body, "reply_markup", markup.toString());
        }
        request("POST", token, "sendMessage", body.toString());
    }

    private TelegramMessage parseMessage(JSONObject messageJson) {
        if (messageJson == null) {
            return null;
        }
        JSONObject chatJson = messageJson.optJSONObject("chat");
        JSONObject fromJson = messageJson.optJSONObject("from");
        return new TelegramMessage(
                messageJson.optLong("message_id", 0L),
                chatJson == null ? 0L : chatJson.optLong("id", 0L),
                fromJson == null ? 0L : fromJson.optLong("id", 0L),
                fromJson == null ? "" : fromJson.optString("first_name", ""),
                fromJson == null ? "" : fromJson.optString("username", ""),
                messageJson.optString("text", "")
        );
    }

    private JSONObject request(String httpMethod, String token, String method, String body)
            throws IOException, TelegramApiException, JSONException {
        String cleanToken = token == null ? "" : token.trim();
        if (cleanToken.isEmpty()) {
            throw new TelegramApiException("Пустой токен");
        }

        URL url = new URL(API_BASE + cleanToken + "/" + method);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestMethod(httpMethod);
        connection.setRequestProperty("User-Agent", "BotForge/0.1");
        connection.setRequestProperty("Accept", "application/json");

        if ("POST".equals(httpMethod)) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            try (OutputStream outputStream = connection.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
                writer.write(body == null ? "" : body);
            }
        }

        int code = connection.getResponseCode();
        InputStream inputStream = code >= 200 && code < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        String raw = readAll(inputStream);
        JSONObject json = new JSONObject(raw);
        if (!json.optBoolean("ok", false)) {
            String description = json.optString("description", "Telegram API вернул ошибку");
            throw new TelegramApiException(description);
        }
        return json;
    }

    private void appendForm(StringBuilder builder, String key, String value) throws IOException {
        if (builder.length() > 0) {
            builder.append('&');
        }
        builder.append(URLEncoder.encode(key, "UTF-8"));
        builder.append('=');
        builder.append(URLEncoder.encode(value == null ? "" : value, "UTF-8"));
    }

    private String readAll(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "{}";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    public static class TelegramApiException extends Exception {
        public TelegramApiException(String message) {
            super(message);
        }
    }
}
