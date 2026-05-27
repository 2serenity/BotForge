package com.rofls.botforge.engine;

import android.content.Context;

import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotResponse;
import com.rofls.botforge.repository.TemplateRepository;
import com.rofls.botforge.storage.StateManager;
import com.rofls.botforge.storage.StorageManager;
import com.rofls.botforge.telegram.TelegramMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class TemplateBotEngine implements BotEngine {
    private final EchoBotEngine echoEngine = new EchoBotEngine();
    private final CommandBotEngine commandEngine = new CommandBotEngine();
    private final StateManager stateManager;
    private final StorageManager storageManager;

    public TemplateBotEngine(Context context) {
        stateManager = new StateManager(context);
        storageManager = new StorageManager(context);
    }

    @Override
    public BotResponse handleMessage(Bot bot, TelegramMessage message) {
        String templateId = bot == null ? "" : bot.getTemplateId();
        if (TemplateRepository.TEMPLATE_ECHO.equals(templateId)) {
            return echoEngine.handleMessage(bot, message);
        }
        if (TemplateRepository.TEMPLATE_COMMAND.equals(templateId)) {
            return commandEngine.handleMessage(bot, message);
        }
        if (TemplateRepository.TEMPLATE_MENU.equals(templateId)) {
            return handleMenu(message);
        }
        if (TemplateRepository.TEMPLATE_FAQ.equals(templateId)) {
            return handleFaq(message);
        }
        if (TemplateRepository.TEMPLATE_BUY_SELL.equals(templateId)) {
            return handleBuySell(bot, message);
        }
        return echoEngine.handleMessage(bot, message);
    }

    private BotResponse handleMenu(TelegramMessage message) {
        String text = normalizedText(message);
        if ("/start".equals(text) || "назад".equals(text)) {
            return new BotResponse("Главное меню", buttons("Каталог", "Помощь"));
        }
        if ("каталог".equals(text)) {
            return new BotResponse("Каталог пока пуст.", buttons("Назад"));
        }
        if ("помощь".equals(text) || "/help".equals(text)) {
            return new BotResponse("Выберите пункт меню или напишите /start.", buttons("Каталог", "Назад"));
        }
        return new BotResponse("Нажмите /start, чтобы открыть меню.", buttons("Главное меню"));
    }

    private BotResponse handleFaq(TelegramMessage message) {
        String text = normalizedText(message);
        if (text.contains("цена")) {
            return new BotResponse("Цена зависит от услуги.");
        }
        if (text.contains("время")) {
            return new BotResponse("Мы работаем каждый день.");
        }
        if (text.contains("контакты")) {
            return new BotResponse("Напишите администратору.");
        }
        return new BotResponse("Не нашёл ответ. Попробуйте спросить иначе.");
    }

    private BotResponse handleBuySell(Bot bot, TelegramMessage message) {
        String text = message == null ? "" : message.getText().trim();
        String normalized = text.toLowerCase(Locale.ROOT);
        String botId = bot == null ? "" : bot.getId();
        long chatId = message == null ? 0L : message.getChatId();
        String state = stateManager.getState(botId, chatId);

        if ("add_title".equals(state)) {
            stateManager.setSessionValue(botId, chatId, "title", text);
            stateManager.setState(botId, chatId, "add_price");
            return new BotResponse("Теперь напиши цену:");
        }

        if ("add_price".equals(state)) {
            JSONObject item = new JSONObject();
            try {
                item.put("title", stateManager.getSessionValue(botId, chatId, "title"));
                item.put("price", text);
                item.put("seller_id", message == null ? 0L : message.getUserId());
            } catch (JSONException ignored) {
            }
            storageManager.add(botId, "items", item);
            stateManager.clearState(botId, chatId);
            stateManager.clearSession(botId, chatId);
            return new BotResponse("Объявление добавлено.", buttons("Купить", "Продать", "Главное меню"));
        }

        if ("/start".equals(normalized) || "главное меню".equals(normalized) || "назад".equals(normalized)) {
            return new BotResponse("Главное меню", buttons("Купить", "Продать", "Мои объявления", "Помощь"));
        }
        if ("купить".equals(normalized)) {
            JSONArray items = storageManager.all(botId, "items");
            if (items.length() == 0) {
                return new BotResponse("Пока объявлений нет.", buttons("Назад"));
            }
            StringBuilder builder = new StringBuilder("Доступные объявления:\n\n");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.optJSONObject(i);
                if (item != null) {
                    builder.append(item.optString("title", "Без названия"))
                            .append(" — ")
                            .append(item.optString("price", "без цены"))
                            .append(" ₽\n");
                }
            }
            return new BotResponse(builder.toString(), buttons("Назад"));
        }
        if ("продать".equals(normalized)) {
            stateManager.setState(botId, chatId, "add_title");
            return new BotResponse("Напиши название товара:");
        }
        if ("мои объявления".equals(normalized)) {
            return new BotResponse("Личный список объявлений появится в следующей версии.", buttons("Назад"));
        }
        if ("помощь".equals(normalized) || "/help".equals(normalized)) {
            return new BotResponse("Этот шаблон помогает собрать простую доску объявлений.", buttons("Назад"));
        }
        return new BotResponse("Выберите пункт меню.", buttons("Купить", "Продать", "Помощь"));
    }

    private String normalizedText(TelegramMessage message) {
        return (message == null ? "" : message.getText().trim()).toLowerCase(Locale.ROOT);
    }

    private JSONArray buttons(String... labels) {
        JSONArray keyboard = new JSONArray();
        for (String label : labels) {
            JSONArray row = new JSONArray();
            row.put(label);
            keyboard.put(row);
        }
        return keyboard;
    }
}
