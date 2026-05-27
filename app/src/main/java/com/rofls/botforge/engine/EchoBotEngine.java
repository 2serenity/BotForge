package com.rofls.botforge.engine;

import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotResponse;
import com.rofls.botforge.telegram.TelegramMessage;

public class EchoBotEngine implements BotEngine {
    @Override
    public BotResponse handleMessage(Bot bot, TelegramMessage message) {
        String text = message == null ? "" : message.getText();
        if (text.trim().isEmpty()) {
            return new BotResponse("Я получил сообщение без текста.");
        }
        return new BotResponse(text);
    }
}
