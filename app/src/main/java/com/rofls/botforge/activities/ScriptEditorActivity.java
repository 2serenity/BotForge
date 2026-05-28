package com.rofls.botforge.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rofls.botforge.R;
import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotMode;
import com.rofls.botforge.repository.BotRepository;
import com.rofls.botforge.repository.LogRepository;
import com.rofls.botforge.repository.ScriptRepository;
import com.rofls.botforge.runner.BotRunnerManager;
import com.rofls.botforge.utils.UiUtils;

public class ScriptEditorActivity extends Activity {
    private String botId;
    private Bot bot;
    private BotRepository botRepository;
    private ScriptRepository scriptRepository;
    private LogRepository logRepository;
    private BotRunnerManager runnerManager;

    private TextView textEditorTitle;
    private EditText inputScript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_editor);

        botId = getIntent().getStringExtra("bot_id");
        botRepository = new BotRepository(this);
        scriptRepository = new ScriptRepository(this);
        logRepository = new LogRepository(this);
        runnerManager = BotRunnerManager.getInstance(this);

        textEditorTitle = findViewById(R.id.textEditorTitle);
        inputScript = findViewById(R.id.inputScript);
        Button buttonSaveScript = findViewById(R.id.buttonSaveScript);
        Button buttonCheckScript = findViewById(R.id.buttonCheckScript);
        Button buttonEditorStart = findViewById(R.id.buttonEditorStart);
        Button buttonEditorStop = findViewById(R.id.buttonEditorStop);
        Button buttonEditorLogs = findViewById(R.id.buttonEditorLogs);
        Button buttonEditorDocs = findViewById(R.id.buttonEditorDocs);

        buttonSaveScript.setOnClickListener(v -> saveScript());
        buttonCheckScript.setOnClickListener(v -> checkScript());
        buttonEditorStart.setOnClickListener(v -> startBotFromEditor());
        buttonEditorStop.setOnClickListener(v -> stopBot());
        buttonEditorLogs.setOnClickListener(v -> openLogs());
        buttonEditorDocs.setOnClickListener(v -> openScriptApiDocs());

        loadBot();
    }

    private void loadBot() {
        bot = botRepository.getBot(botId);
        if (bot == null) {
            finish();
            return;
        }
        textEditorTitle.setText("Скрипт: " + bot.getName());
        inputScript.setText(scriptRepository.getScript(bot.getScriptId()));
    }

    private void saveScript() {
        if (bot == null) {
            return;
        }
        scriptRepository.saveScript(bot.getScriptId(), inputScript.getText().toString());
        logRepository.info(bot, "Скрипт сохранён");
        UiUtils.toast(this, "Скрипт сохранён");
    }

    private void checkScript() {
        String code = inputScript.getText().toString();
        if (isBasicScriptValid(code)) {
            UiUtils.showInfo(this, "Проверка", "Базовая проверка пройдена.");
        } else {
            UiUtils.showError(this, "Код не должен быть пустым и должен содержать @bot.command, @bot.message или def.");
        }
    }

    private boolean isBasicScriptValid(String code) {
        String trimmed = code == null ? "" : code.trim();
        return !trimmed.isEmpty()
                && (trimmed.contains("@bot.command")
                || trimmed.contains("@bot.message")
                || trimmed.contains("def "));
    }

    private void startBotFromEditor() {
        if (bot == null) {
            return;
        }
        saveScript();
        if (bot.getMode() == BotMode.DEVELOPER && !isBasicScriptValid(inputScript.getText().toString())) {
            UiUtils.showError(this, "Сначала исправьте базовые ошибки скрипта.");
            return;
        }
        if (!UiUtils.hasNetwork(this)) {
            logRepository.warn(bot, "Запуск из редактора отменён: нет интернета", "");
            UiUtils.showError(this, "Нет подключения к интернету.");
            return;
        }
        if (bot.getMode() == BotMode.DEVELOPER && bot.getLastStartedAt() == 0L) {
            new AlertDialog.Builder(this)
                    .setTitle("Developer Mode")
                    .setMessage("Вы запускаете пользовательский Python-код локально на своём устройстве. Код может содержать ошибки, зависать, потреблять ресурсы устройства и нарушать работу бота. Вы несёте ответственность за скрипт, который запускаете.")
                    .setPositiveButton("Запустить", (dialog, which) -> runStart())
                    .setNegativeButton("Отмена", null)
                    .show();
            return;
        }
        runStart();
    }

    private void runStart() {
        try {
            runnerManager.startBot(bot);
            UiUtils.toast(this, "Polling запущен");
            bot = botRepository.getBot(botId);
        } catch (IllegalStateException ex) {
            UiUtils.showError(this, ex.getMessage());
        }
    }

    private void stopBot() {
        if (bot == null) {
            return;
        }
        runnerManager.stopBot(bot.getId());
        UiUtils.toast(this, "Polling остановлен");
    }

    private void openLogs() {
        Intent intent = new Intent(this, LogsActivity.class);
        intent.putExtra("bot_id", botId);
        startActivity(intent);
    }

    private void openScriptApiDocs() {
        startActivity(new Intent(this, ScriptApiDocsActivity.class));
    }
}
