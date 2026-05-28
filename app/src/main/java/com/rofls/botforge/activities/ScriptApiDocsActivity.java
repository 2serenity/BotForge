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
        return "Статус: первая исполняемая версия API для режима разработчика.\n\n"
                + "Python-скрипты выполняются локально через Chaquopy. API намеренно небольшой, "
                + "чтобы сначала стабилизировать раннер и связку Java/Python.\n\n"
                + "Главное правило: Python-код не получает Telegram-токен. Android отвечает за токен, опрос, offset и Telegram Bot API.\n\n"
                + "Минимальный бот:\n\n"
                + "from botforge import bot\n\n"
                + "@bot.message()\n"
                + "def echo(ctx):\n"
                + "    ctx.reply(ctx.text)\n\n"
                + "Декораторы:\n\n"
                + "@bot.message()\n"
                + "Обработчик любого текстового сообщения, если не найден более точный обработчик.\n\n"
                + "@bot.command(\"/start\")\n"
                + "Обработчик команды Telegram. Сравнение команды точное и зависит от регистра.\n\n"
                + "@bot.button(\"Каталог\")\n"
                + "Обработчик нажатия кнопки. В первой версии связки это работает как сравнение текста кнопки с ctx.text.\n\n"
                + "@bot.state(\"state_name\")\n"
                + "Обработчик сообщения, когда текущий чат находится в указанном состоянии.\n\n"
                + "Поля контекста:\n\n"
                + "ctx.text - текст входящего сообщения\n"
                + "ctx.chat_id - Telegram chat id\n"
                + "ctx.user_id - Telegram user id\n"
                + "ctx.username - Telegram username, если есть\n"
                + "ctx.first_name - имя пользователя Telegram, если есть\n"
                + "ctx.message_id - Telegram message id\n"
                + "ctx.session - временный словарь для пары бот-чат\n"
                + "ctx.storage - локальное хранилище бота\n\n"
                + "Методы контекста:\n\n"
                + "ctx.reply(text)\n"
                + "ctx.reply(text, buttons=[[\"Да\"], [\"Нет\"]])\n"
                + "ctx.set_state(\"state_name\")\n"
                + "ctx.clear_state()\n"
                + "ctx.get_state()\n\n"
                + "Кнопки:\n\n"
                + "buttons=[\n"
                + "    [\"Каталог\"],\n"
                + "    [\"Помощь\", \"Назад\"]\n"
                + "]\n\n"
                + "Хранилище:\n\n"
                + "ctx.storage.add(\"items\", {\n"
                + "    \"title\": \"Телефон\",\n"
                + "    \"price\": \"10000\",\n"
                + "    \"seller_id\": ctx.user_id\n"
                + "})\n\n"
                + "items = ctx.storage.all(\"items\")\n"
                + "ctx.storage.clear(\"items\")\n\n"
                + "Приоритет обработчиков:\n\n"
                + "1. Текущий @bot.state(...)\n"
                + "2. Точное совпадение @bot.command(...)\n"
                + "3. Точное совпадение @bot.button(...)\n"
                + "4. Запасной @bot.message()\n\n"
                + "Ограничения выполнения:\n\n"
                + "BotForge - локальный мобильный раннер, не VPS. Android может остановить фоновые потоки, "
                + "ограничить сеть или применить экономию батареи. Скрипты не должны зависать или блокировать поток опроса.\n";
    }
}
