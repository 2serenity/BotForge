package com.rofls.botforge.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.rofls.botforge.R;
import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotMode;
import com.rofls.botforge.models.BotStatus;
import com.rofls.botforge.models.BotTemplate;
import com.rofls.botforge.repository.BotRepository;
import com.rofls.botforge.repository.LogRepository;
import com.rofls.botforge.repository.ScriptRepository;
import com.rofls.botforge.repository.TemplateRepository;
import com.rofls.botforge.telegram.TelegramApiClient;
import com.rofls.botforge.utils.UiUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddBotActivity extends Activity {
    private EditText inputBotName;
    private EditText inputToken;
    private RadioGroup radioMode;
    private RadioButton radioTemplate;
    private Spinner spinnerTemplates;
    private TextView textTemplateLabel;
    private TextView textTokenResult;
    private Button buttonCheckToken;
    private Button buttonSaveBot;

    private BotRepository botRepository;
    private ScriptRepository scriptRepository;
    private LogRepository logRepository;
    private TemplateRepository templateRepository;
    private TelegramApiClient telegramApiClient;
    private ExecutorService executorService;
    private List<BotTemplate> templates;

    private String verifiedToken = "";
    private String verifiedUsername = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bot);

        botRepository = new BotRepository(this);
        scriptRepository = new ScriptRepository(this);
        logRepository = new LogRepository(this);
        templateRepository = new TemplateRepository();
        telegramApiClient = new TelegramApiClient();
        executorService = Executors.newSingleThreadExecutor();

        inputBotName = findViewById(R.id.inputBotName);
        inputToken = findViewById(R.id.inputToken);
        radioMode = findViewById(R.id.radioMode);
        radioTemplate = findViewById(R.id.radioTemplate);
        spinnerTemplates = findViewById(R.id.spinnerTemplates);
        textTemplateLabel = findViewById(R.id.textTemplateLabel);
        textTokenResult = findViewById(R.id.textTokenResult);
        buttonCheckToken = findViewById(R.id.buttonCheckToken);
        buttonSaveBot = findViewById(R.id.buttonSaveBot);

        setupTemplates();
        setupInitialMode();

        radioMode.setOnCheckedChangeListener((group, checkedId) -> updateTemplateVisibility());
        buttonCheckToken.setOnClickListener(v -> checkToken());
        buttonSaveBot.setOnClickListener(v -> saveBot());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    private void setupTemplates() {
        templates = templateRepository.getTemplates();
        ArrayAdapter<BotTemplate> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, templates);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerTemplates.setAdapter(adapter);
    }

    private void setupInitialMode() {
        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        String templateId = intent.getStringExtra("template_id");
        if ("TEMPLATE".equals(mode)) {
            radioTemplate.setChecked(true);
        }
        if (templateId != null) {
            for (int i = 0; i < templates.size(); i++) {
                if (templateId.equals(templates.get(i).getId())) {
                    spinnerTemplates.setSelection(i);
                    break;
                }
            }
        }
        updateTemplateVisibility();
    }

    private void updateTemplateVisibility() {
        boolean templateMode = radioTemplate.isChecked();
        textTemplateLabel.setVisibility(templateMode ? View.VISIBLE : View.GONE);
        spinnerTemplates.setVisibility(templateMode ? View.VISIBLE : View.GONE);
    }

    private void checkToken() {
        String token = inputToken.getText().toString().trim();
        if (token.isEmpty()) {
            UiUtils.showError(this, "Введите токен Telegram-бота.");
            return;
        }
        if (!UiUtils.hasNetwork(this)) {
            UiUtils.showError(this, "Нет подключения к интернету.");
            return;
        }

        buttonCheckToken.setEnabled(false);
        buttonCheckToken.setText("Проверяем...");
        textTokenResult.setText("Запрос getMe...");
        textTokenResult.setTextColor(getColor(R.color.bf_text_secondary));

        executorService.execute(() -> {
            try {
                String username = telegramApiClient.getMe(token);
                runOnUiThread(() -> {
                    verifiedToken = token;
                    verifiedUsername = username;
                    textTokenResult.setText("Токен валиден: @" + username);
                    textTokenResult.setTextColor(getColor(R.color.bf_success));
                    buttonCheckToken.setEnabled(true);
                    buttonCheckToken.setText("Проверить токен");
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    verifiedToken = "";
                    verifiedUsername = "";
                    textTokenResult.setText("Ошибка: " + ex.getMessage());
                    textTokenResult.setTextColor(getColor(R.color.bf_error));
                    buttonCheckToken.setEnabled(true);
                    buttonCheckToken.setText("Проверить токен");
                    UiUtils.showError(this, ex.getMessage());
                });
            }
        });
    }

    private void saveBot() {
        String name = inputBotName.getText().toString().trim();
        String token = inputToken.getText().toString().trim();

        if (name.isEmpty()) {
            UiUtils.showError(this, "Введите название бота.");
            return;
        }
        if (token.isEmpty()) {
            UiUtils.showError(this, "Введите токен Telegram-бота.");
            return;
        }
        if (!token.equals(verifiedToken) || verifiedUsername.isEmpty()) {
            UiUtils.showError(this, "Сначала проверьте токен через getMe.");
            return;
        }

        Bot bot = new Bot();
        bot.setName(name);
        bot.setToken(token);
        bot.setUsername(verifiedUsername);
        bot.setStatus(BotStatus.STOPPED);
        bot.setMode(radioTemplate.isChecked() ? BotMode.TEMPLATE : BotMode.DEVELOPER);

        String initialScript;
        if (bot.getMode() == BotMode.TEMPLATE) {
            BotTemplate selected = (BotTemplate) spinnerTemplates.getSelectedItem();
            bot.setTemplateId(selected.getId());
            initialScript = selected.getDefaultScript();
        } else {
            bot.setTemplateId("");
            initialScript = developerStarterScript();
        }

        botRepository.saveBot(bot);
        scriptRepository.saveScript(bot.getScriptId(), initialScript);
        logRepository.info(bot, "Бот сохранён локально");
        UiUtils.toast(this, "Бот сохранён");

        Intent intent = new Intent(this, BotDetailActivity.class);
        intent.putExtra("bot_id", bot.getId());
        startActivity(intent);
        finish();
    }

    private String developerStarterScript() {
        return "from botforge import bot\n\n"
                + "@bot.message()\n"
                + "def handle(ctx):\n"
                + "    ctx.reply(\"Скрипт сохранён в BotForge\")\n";
    }
}
