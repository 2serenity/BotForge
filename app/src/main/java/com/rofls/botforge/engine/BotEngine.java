package com.rofls.botforge.engine;

import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotResponse;
import com.rofls.botforge.telegram.TelegramMessage;

public interface BotEngine {
    BotResponse handleMessage(Bot bot, TelegramMessage message);
}
