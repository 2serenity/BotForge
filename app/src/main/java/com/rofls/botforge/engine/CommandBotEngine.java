package com.rofls.botforge.engine;

import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotResponse;
import com.rofls.botforge.telegram.TelegramMessage;

public class CommandBotEngine implements BotEngine {
    @Override
    public BotResponse handleMessage(Bot bot, TelegramMessage message) {
        String text = message == null ? "" : message.getText().trim();
        if ("/start".equals(text)) {
            return new BotResponse("Привет! Я бот, запущенный через BotForge.");
        }
        if ("/help".equals(text)) {
            return new BotResponse("Доступные команды: /start, /help");
        }
        return new BotResponse("Неизвестная команда. Напишите /help.");
    }
}
