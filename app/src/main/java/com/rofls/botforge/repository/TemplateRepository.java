package com.rofls.botforge.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.rofls.botforge.models.BotTemplate;
import com.rofls.botforge.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TemplateRepository {
    public static final String TEMPLATE_ECHO = "echo";
    public static final String TEMPLATE_COMMAND = "command";
    public static final String TEMPLATE_MENU = "menu";
    public static final String TEMPLATE_FAQ = "faq";
    public static final String TEMPLATE_BUY_SELL = "buy_sell";

    private static final String PREFS_NAME = "botforge_custom_templates";
    private static final String KEY_TEMPLATES = "templates";
    private static final String CUSTOM_PREFIX = "custom_";

    private final SharedPreferences prefs;

    public TemplateRepository() {
        this.prefs = null;
    }

    public TemplateRepository(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<BotTemplate> getTemplates() {
        List<BotTemplate> templates = getBuiltInTemplates();
        templates.addAll(getCustomTemplates());
        return templates;
    }

    public List<BotTemplate> getBuiltInTemplates() {
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

    public List<BotTemplate> getCustomTemplates() {
        List<BotTemplate> templates = new ArrayList<>();
        if (prefs == null) {
            return templates;
        }

        JSONArray array = JsonUtils.safeArray(prefs.getString(KEY_TEMPLATES, "[]"));
        for (int i = 0; i < array.length(); i++) {
            BotTemplate template = BotTemplate.fromJson(array.optJSONObject(i));
            if (template.getId() != null && !template.getId().trim().isEmpty()) {
                template.setBuiltIn(false);
                templates.add(template);
            }
        }
        return templates;
    }

    public BotTemplate getTemplate(String id) {
        BotTemplate template = findTemplate(id);
        if (template != null) {
            return template;
        }
        return getTemplates().get(0);
    }

    public BotTemplate findTemplate(String id) {
        if (id == null) {
            return null;
        }
        for (BotTemplate template : getTemplates()) {
            if (template.getId().equals(id)) {
                return template;
            }
        }
        return null;
    }

    public static boolean isCustomTemplateId(String templateId) {
        return templateId != null && templateId.startsWith(CUSTOM_PREFIX);
    }

    public BotTemplate saveCustomTemplate(BotTemplate template) {
        if (prefs == null) {
            throw new IllegalStateException("Custom templates require a Context-backed TemplateRepository");
        }

        long now = System.currentTimeMillis();
        if (template.getId() == null || template.getId().trim().isEmpty()) {
            template.setId(CUSTOM_PREFIX + UUID.randomUUID());
            template.setCreatedAt(now);
        }
        if (template.getCreatedAt() <= 0L) {
            template.setCreatedAt(now);
        }
        template.setBuiltIn(false);
        template.setUpdatedAt(now);

        List<BotTemplate> templates = getCustomTemplates();
        boolean replaced = false;
        for (int i = 0; i < templates.size(); i++) {
            if (template.getId().equals(templates.get(i).getId())) {
                templates.set(i, template);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            templates.add(template);
        }
        writeCustomTemplates(templates);
        return template;
    }

    public void deleteCustomTemplate(String templateId) {
        if (prefs == null || templateId == null) {
            return;
        }

        List<BotTemplate> remaining = new ArrayList<>();
        for (BotTemplate template : getCustomTemplates()) {
            if (!templateId.equals(template.getId())) {
                remaining.add(template);
            }
        }
        writeCustomTemplates(remaining);
    }

    private void writeCustomTemplates(List<BotTemplate> templates) {
        JSONArray array = new JSONArray();
        for (BotTemplate template : templates) {
            try {
                array.put(template.toJson());
            } catch (JSONException ignored) {
                // Skip broken user template records.
            }
        }
        prefs.edit().putString(KEY_TEMPLATES, array.toString()).apply();
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
