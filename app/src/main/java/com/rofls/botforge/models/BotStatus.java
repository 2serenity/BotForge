package com.rofls.botforge.models;

public enum BotStatus {
    STOPPED,
    RUNNING,
    ERROR;

    public static BotStatus fromString(String value) {
        if (value == null) {
            return STOPPED;
        }
        try {
            return BotStatus.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return STOPPED;
        }
    }
}
