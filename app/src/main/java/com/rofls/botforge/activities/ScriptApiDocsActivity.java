package com.rofls.botforge.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.rofls.botforge.R;

public class ScriptApiDocsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_api_docs);

        TextView textDocs = findViewById(R.id.textScriptApiDocs);
        textDocs.setText(buildDocs());
    }

    private String buildDocs() {
        return "Status: first executable API for Developer Mode.\n\n"
                + "Python-скрипты выполняются локально через Chaquopy. API намеренно небольшой, "
                + "чтобы сначала стабилизировать runner и bridge.\n\n"
                + "Главное правило: Python-код не получает Telegram token. Android отвечает за token, polling, offset и Telegram Bot API.\n\n"
                + "Минимальный бот:\n\n"
                + "from botforge import bot\n\n"
                + "@bot.message()\n"
                + "def echo(ctx):\n"
                + "    ctx.reply(ctx.text)\n\n"
                + "Decorators:\n\n"
                + "@bot.message()\n"
                + "Обработчик любого текстового сообщения, если не найден более точный handler.\n\n"
                + "@bot.command(\"/start\")\n"
                + "Обработчик команды Telegram. Сравнение команды должно быть точным.\n\n"
                + "@bot.button(\"Catalog\")\n"
                + "Обработчик нажатия кнопки. В первой версии bridge это можно реализовать как matching по ctx.text.\n\n"
                + "@bot.state(\"state_name\")\n"
                + "Обработчик сообщения, когда текущий чат находится в указанном состоянии.\n\n"
                + "Context fields:\n\n"
                + "ctx.text - текст входящего сообщения\n"
                + "ctx.chat_id - Telegram chat id\n"
                + "ctx.user_id - Telegram user id\n"
                + "ctx.username - username, если есть\n"
                + "ctx.first_name - first name, если есть\n"
                + "ctx.message_id - message id\n"
                + "ctx.session - временный per-chat словарь\n"
                + "ctx.storage - локальное per-bot хранилище\n\n"
                + "Context methods:\n\n"
                + "ctx.reply(text)\n"
                + "ctx.reply(text, buttons=[[\"A\"], [\"B\"]])\n"
                + "ctx.set_state(\"state_name\")\n"
                + "ctx.clear_state()\n"
                + "ctx.get_state()\n\n"
                + "Buttons:\n\n"
                + "buttons=[\n"
                + "    [\"Catalog\"],\n"
                + "    [\"Help\", \"Back\"]\n"
                + "]\n\n"
                + "Storage draft:\n\n"
                + "ctx.storage.add(\"items\", {\n"
                + "    \"title\": \"Phone\",\n"
                + "    \"price\": \"10000\",\n"
                + "    \"seller_id\": ctx.user_id\n"
                + "})\n\n"
                + "items = ctx.storage.all(\"items\")\n"
                + "ctx.storage.clear(\"items\")\n\n"
                + "Recommended handler priority:\n\n"
                + "1. Current @bot.state(...)\n"
                + "2. Exact @bot.command(...)\n"
                + "3. Exact @bot.button(...)\n"
                + "4. Fallback @bot.message()\n\n"
                + "Runtime limits:\n\n"
                + "BotForge - локальный мобильный раннер, не VPS. Android может остановить фоновые потоки, "
                + "ограничить сеть или применить экономию батареи. Скрипты не должны зависать или блокировать polling thread.\n";
    }
}
