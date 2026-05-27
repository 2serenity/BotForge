package com.rofls.botforge.repository;

import com.rofls.botforge.models.BotTemplate;

import java.util.ArrayList;
import java.util.List;

public class TemplateRepository {
    public static final String TEMPLATE_ECHO = "echo";
    public static final String TEMPLATE_COMMAND = "command";
    public static final String TEMPLATE_MENU = "menu";
    public static final String TEMPLATE_FAQ = "faq";
    public static final String TEMPLATE_BUY_SELL = "buy_sell";

    public List<BotTemplate> getTemplates() {
        List<BotTemplate> templates = new ArrayList<>();
        templates.add(new BotTemplate(
                TEMPLATE_ECHO,
                "Echo Bot",
                "Повторяет любое входящее сообщение.",
                "Лёгкий",
                echoScript()
        ));
        templates.add(new BotTemplate(
                TEMPLATE_COMMAND,
                "Command Bot",
                "Отвечает на /start и /help.",
                "Лёгкий",
                commandScript()
        ));
        templates.add(new BotTemplate(
                TEMPLATE_MENU,
                "Menu Bot",
                "Показывает простое меню с кнопками.",
                "Средний",
                menuScript()
        ));
        templates.add(new BotTemplate(
                TEMPLATE_FAQ,
                "FAQ Bot",
                "Ищет ответы по ключевым словам.",
                "Средний",
                faqScript()
        ));
        templates.add(new BotTemplate(
                TEMPLATE_BUY_SELL,
                "Buy/Sell Bot",
                "Заготовка для доски объявлений.",
                "Сложный",
                buySellScript()
        ));
        return templates;
    }

    public BotTemplate getTemplate(String id) {
        for (BotTemplate template : getTemplates()) {
            if (template.getId().equals(id)) {
                return template;
            }
        }
        return getTemplates().get(0);
    }

    private String echoScript() {
        return "from botforge import bot\n\n"
                + "@bot.message()\n"
                + "def echo(ctx):\n"
                + "    ctx.reply(ctx.text)\n";
    }

    private String commandScript() {
        return "from botforge import bot\n\n"
                + "@bot.command(\"/start\")\n"
                + "def start(ctx):\n"
                + "    ctx.reply(\"Привет! Я бот, запущенный через BotForge.\")\n\n"
                + "@bot.command(\"/help\")\n"
                + "def help(ctx):\n"
                + "    ctx.reply(\"Доступные команды: /start, /help\")\n";
    }

    private String menuScript() {
        return "from botforge import bot\n\n"
                + "@bot.command(\"/start\")\n"
                + "def start(ctx):\n"
                + "    ctx.reply(\"Главное меню\", buttons=[\n"
                + "        [\"Каталог\"],\n"
                + "        [\"Помощь\"]\n"
                + "    ])\n\n"
                + "@bot.button(\"Каталог\")\n"
                + "def catalog(ctx):\n"
                + "    ctx.reply(\"Каталог пока пуст.\", buttons=[\n"
                + "        [\"Назад\"]\n"
                + "    ])\n";
    }

    private String faqScript() {
        return "from botforge import bot\n\n"
                + "FAQ = {\n"
                + "    \"цена\": \"Цена зависит от услуги.\",\n"
                + "    \"время\": \"Мы работаем каждый день.\",\n"
                + "    \"контакты\": \"Напишите администратору.\"\n"
                + "}\n\n"
                + "@bot.message()\n"
                + "def faq(ctx):\n"
                + "    text = ctx.text.lower()\n"
                + "    for key in FAQ:\n"
                + "        if key in text:\n"
                + "            ctx.reply(FAQ[key])\n"
                + "            return\n"
                + "    ctx.reply(\"Не нашёл ответ. Попробуйте спросить иначе.\")\n";
    }

    private String buySellScript() {
        return "from botforge import bot\n\n"
                + "@bot.command(\"/start\")\n"
                + "def start(ctx):\n"
                + "    ctx.reply(\"Главное меню\", buttons=[\n"
                + "        [\"Купить\"],\n"
                + "        [\"Продать\"],\n"
                + "        [\"Мои объявления\"],\n"
                + "        [\"Помощь\"]\n"
                + "    ])\n\n"
                + "@bot.button(\"Купить\")\n"
                + "def buy(ctx):\n"
                + "    items = ctx.storage.all(\"items\")\n\n"
                + "    if not items:\n"
                + "        ctx.reply(\"Пока объявлений нет.\", buttons=[\n"
                + "            [\"Назад\"]\n"
                + "        ])\n"
                + "        return\n\n"
                + "    text = \"Доступные объявления:\\\\n\\\\n\"\n"
                + "    for item in items:\n"
                + "        text += f\"{item['title']} — {item['price']} ₽\\\\n\"\n\n"
                + "    ctx.reply(text, buttons=[\n"
                + "        [\"Назад\"]\n"
                + "    ])\n\n"
                + "@bot.button(\"Продать\")\n"
                + "def sell(ctx):\n"
                + "    ctx.set_state(\"add_title\")\n"
                + "    ctx.reply(\"Напиши название товара:\")\n\n"
                + "@bot.state(\"add_title\")\n"
                + "def add_title(ctx):\n"
                + "    ctx.session[\"title\"] = ctx.text\n"
                + "    ctx.set_state(\"add_price\")\n"
                + "    ctx.reply(\"Теперь напиши цену:\")\n\n"
                + "@bot.state(\"add_price\")\n"
                + "def add_price(ctx):\n"
                + "    ctx.storage.add(\"items\", {\n"
                + "        \"title\": ctx.session[\"title\"],\n"
                + "        \"price\": ctx.text,\n"
                + "        \"seller_id\": ctx.user_id\n"
                + "    })\n\n"
                + "    ctx.clear_state()\n"
                + "    ctx.reply(\"Объявление добавлено.\", buttons=[\n"
                + "        [\"Купить\"],\n"
                + "        [\"Продать\"],\n"
                + "        [\"Главное меню\"]\n"
                + "    ])\n";
    }
}
