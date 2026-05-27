package com.rofls.botforge.telegram;

public class TelegramUpdate {
    private final long updateId;
    private final TelegramMessage message;

    public TelegramUpdate(long updateId, TelegramMessage message) {
        this.updateId = updateId;
        this.message = message;
    }

    public long getUpdateId() {
        return updateId;
    }

    public TelegramMessage getMessage() {
        return message;
    }
}
