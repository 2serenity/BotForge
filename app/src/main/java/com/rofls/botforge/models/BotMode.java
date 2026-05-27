package com.rofls.botforge.models;

public enum BotMode {
    TEMPLATE,
    DEVELOPER;

    public static BotMode fromString(String value) {
        if (value == null) {
            return TEMPLATE;
        }
        try {
            return BotMode.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return TEMPLATE;
        }
    }
}
