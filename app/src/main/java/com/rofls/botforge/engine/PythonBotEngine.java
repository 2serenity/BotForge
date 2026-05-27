package com.rofls.botforge.engine;

import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotResponse;
import com.rofls.botforge.repository.ScriptRepository;
import com.rofls.botforge.telegram.TelegramMessage;

public class PythonBotEngine implements BotEngine {
    private final ScriptRepository scriptRepository;

    public PythonBotEngine(ScriptRepository scriptRepository) {
        this.scriptRepository = scriptRepository;
    }

    @Override
    public BotResponse handleMessage(Bot bot, TelegramMessage message) {
        String script = bot == null ? "" : scriptRepository.getScript(bot.getScriptId());
        if (script.trim().isEmpty()) {
            return new BotResponse("Скрипт пустой. Откройте редактор BotForge и сохраните код.");
        }

        // TODO: Replace this stub with a Chaquopy-backed bridge.
        // Planned bridge shape:
        // 1. Android keeps Telegram token, polling, update_id and sendMessage.
        // 2. Python receives only a safe BotContext-like object without token.
        // 3. Python returns BotResponse text/buttons to Java.
        return new BotResponse("Python engine пока в режиме заглушки. Скрипт сохранён.");
    }
}
