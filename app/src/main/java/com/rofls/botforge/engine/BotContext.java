package com.rofls.botforge.engine;

import com.rofls.botforge.models.Bot;
import com.rofls.botforge.telegram.TelegramMessage;

public class BotContext {
    private final Bot bot;
    private final TelegramMessage message;

    public BotContext(Bot bot, TelegramMessage message) {
        this.bot = bot;
        this.message = message;
    }

    public Bot getBot() {
        return bot;
    }

    public TelegramMessage getMessage() {
        return message;
    }

    public String getText() {
        return message == null ? "" : message.getText();
    }

    public long getChatId() {
        return message == null ? 0L : message.getChatId();
    }

    public long getUserId() {
        return message == null ? 0L : message.getUserId();
    }
}
