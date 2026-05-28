package com.rofls.botforge.engine;

import android.content.Context;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotResponse;
import com.rofls.botforge.repository.LogRepository;
import com.rofls.botforge.repository.ScriptRepository;
import com.rofls.botforge.storage.StateManager;
import com.rofls.botforge.storage.StorageManager;
import com.rofls.botforge.telegram.TelegramMessage;

import org.json.JSONArray;
import org.json.JSONObject;

public class PythonBotEngine implements BotEngine {
    private final Context appContext;
    private final ScriptRepository scriptRepository;
    private final LogRepository logRepository;
    private final StateManager stateManager;
    private final StorageManager storageManager;

    public PythonBotEngine(Context context, ScriptRepository scriptRepository, LogRepository logRepository) {
        this.appContext = context.getApplicationContext();
        this.scriptRepository = scriptRepository;
        this.logRepository = logRepository;
        this.stateManager = new StateManager(appContext);
        this.storageManager = new StorageManager(appContext);
    }

    @Override
    public BotResponse handleMessage(Bot bot, TelegramMessage message) {
        String script = bot == null ? "" : scriptRepository.getScript(bot.getScriptId());
        if (script.trim().isEmpty()) {
            return new BotResponse("Скрипт пустой. Откройте редактор BotForge и сохраните код.");
        }

        try {
            ensurePythonStarted();
            JSONObject data = buildMessageData(bot, message);
            PythonBotBridge bridge = new PythonBotBridge(bot, message, stateManager, storageManager);

            PyObject runtime = Python.getInstance().getModule("botforge_runtime");
            String rawResult = runtime.callAttr("handle_message", script, data.toString(), bridge).toString();
            JSONObject result = new JSONObject(rawResult);

            if (result.has("error")) {
                String error = result.optString("error", "Неизвестная ошибка Python");
                logRepository.error(bot, "Ошибка Python-скрипта", error);
                return new BotResponse("Ошибка Python-скрипта. Откройте логи BotForge.");
            }

            String text = result.optString("text", "");
            JSONArray buttons = result.optJSONArray("buttons");
            return new BotResponse(text, buttons);
        } catch (Exception ex) {
            logRepository.error(bot, "Ошибка Python-движка", ex.getMessage());
            return new BotResponse("Ошибка Python-движка. Откройте логи BotForge.");
        }
    }

    private void ensurePythonStarted() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(appContext));
        }
    }

    private JSONObject buildMessageData(Bot bot, TelegramMessage message) throws Exception {
        JSONObject data = new JSONObject();
        data.put("bot_id", bot.getId());
        data.put("bot_name", bot.getName());
        data.put("template_id", bot.getTemplateId());
        data.put("message_id", message.getMessageId());
        data.put("chat_id", message.getChatId());
        data.put("user_id", message.getUserId());
        data.put("username", message.getUsername());
        data.put("first_name", message.getFirstName());
        data.put("text", message.getText());
        return data;
    }
}
