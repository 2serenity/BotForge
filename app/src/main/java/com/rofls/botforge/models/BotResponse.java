package com.rofls.botforge.models;

import org.json.JSONArray;

public class BotResponse {
    private final String text;
    private final JSONArray buttons;

    public BotResponse(String text) {
        this(text, null);
    }

    public BotResponse(String text, JSONArray buttons) {
        this.text = text;
        this.buttons = buttons;
    }

    public String getText() {
        return text;
    }

    public JSONArray getButtons() {
        return buttons;
    }

    public boolean hasText() {
        return text != null && !text.trim().isEmpty();
    }
}
