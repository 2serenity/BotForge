package com.rofls.botforge.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.rofls.botforge.R;
import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotMode;
import com.rofls.botforge.models.BotStatus;
import com.rofls.botforge.models.BotTemplate;
import com.rofls.botforge.models.LogEntry;
import com.rofls.botforge.repository.BotRepository;
import com.rofls.botforge.repository.LogRepository;
import com.rofls.botforge.repository.ScriptRepository;
import com.rofls.botforge.repository.TemplateRepository;
import com.rofls.botforge.runner.BotRunnerManager;
import com.rofls.botforge.utils.DateUtils;
import com.rofls.botforge.utils.UiUtils;

public class BotDetailActivity extends Activity {
    private String botId;
    private Bot bot;
    private BotRepository botRepository;
    private ScriptRepository scriptRepository;
    private LogRepository logRepository;
    private TemplateRepository templateRepository;
    private BotRunnerManager runnerManager;

    private TextView textBotTitle;
    private TextView textBotInfo;
    private Button buttonStartBot;
    private Button buttonStopBot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_detail);

        botId = getIntent().getStringExtra("bot_id");
        botRepository = new BotRepository(this);
        scriptRepository = new ScriptRepository(this);
        logRepository = new LogRepository(this);
        templateRepository = new TemplateRepository();
        runnerManager = BotRunnerManager.getInstance(this);

        textBotTitle = findViewById(R.id.textBotTitle);
        textBotInfo = findViewById(R.id.textBotInfo);
        buttonStartBot = findViewById(R.id.buttonStartBot);
        buttonStopBot = findViewById(R.id.buttonStopBot);
        Button buttonEditScript = findViewById(R.id.buttonEditScript);
        Button buttonBotLogs = findViewById(R.id.buttonBotLogs);
        Button buttonResetOffset = findViewById(R.id.buttonResetOffset);
        Button buttonDeleteBot = findViewById(R.id.buttonDeleteBot);

        buttonStartBot.setOnClickListener(v -> startBotWithChecks());
        buttonStopBot.setOnClickListener(v -> stopBot());
        buttonEditScript.setOnClickListener(v -> openScriptEditor());
        buttonBotLogs.setOnClickListener(v -> openLogs());
        buttonResetOffset.setOnClickListener(v -> confirmResetOffset());
        buttonDeleteBot.setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBot();
    }

    private void refreshBot() {
        bot = botRepository.getBot(botId);
        if (bot == null) {
            finish();
            return;
        }
        if (runnerManager.isRunning(bot.getId())) {
            bot.setStatus(BotStatus.RUNNING);
        } else if (bot.getStatus() == BotStatus.RUNNING) {
            bot.setStatus(BotStatus.STOPPED);
            botRepository.saveBot(bot);
        }

        textBotTitle.setText(bot.getName());
        BotTemplate template = templateRepository.getTemplate(bot.getTemplateId());
        String templateText = bot.getMode() == BotMode.TEMPLATE ? template.getName() : "Developer Mode";
        String info = "Username: @" + bot.getUsername()
                + "\nСтатус: " + bot.getStatus().name()
                + "\nРежим: " + bot.getMode().name()
                + "\nШаблон: " + templateText
                + "\nСоздан: " + DateUtils.format(bot.getCreatedAt())
                + "\nПоследний запуск: " + DateUtils.format(bot.getLastStartedAt())
                + "\nLast update id: " + bot.getLastUpdateId();
        if (bot.getStatus() == BotStatus.ERROR) {
            String lastError = findLastErrorMessage();
            if (!lastError.isEmpty()) {
                info += "\nПоследняя ошибка: " + lastError;
            }
        }
        textBotInfo.setText(info);

        boolean running = runnerManager.isRunning(bot.getId());
        buttonStartBot.setEnabled(!running);
        buttonStopBot.setEnabled(running);
    }

    private void startBotWithChecks() {
        if (bot == null) {
            return;
        }
        if (!UiUtils.hasNetwork(this)) {
            logRepository.warn(bot, "Запуск отменён: нет интернета", "");
            UiUtils.showError(this, "Нет подключения к интернету.");
            return;
        }
        if (bot.getMode() == BotMode.DEVELOPER && bot.getLastStartedAt() == 0L) {
            showDeveloperWarningThenStart();
            return;
        }
        runStart();
    }

    private void showDeveloperWarningThenStart() {
        new AlertDialog.Builder(this)
                .setTitle("Developer Mode")
                .setMessage("Вы запускаете пользовательский Python-код локально на своём устройстве. Код может содержать ошибки, зависать, потреблять ресурсы устройства и нарушать работу бота. Вы несёте ответственность за скрипт, который запускаете.")
                .setPositiveButton("Запустить", (dialog, which) -> runStart())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void runStart() {
        try {
            runnerManager.startBot(bot);
            UiUtils.toast(this, "Polling запущен");
            refreshBot();
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
        refreshBot();
    }

    private void openScriptEditor() {
        Intent intent = new Intent(this, ScriptEditorActivity.class);
        intent.putExtra("bot_id", botId);
        startActivity(intent);
    }

    private void openLogs() {
        Intent intent = new Intent(this, LogsActivity.class);
        intent.putExtra("bot_id", botId);
        startActivity(intent);
    }

    private String findLastErrorMessage() {
        for (LogEntry entry : logRepository.getLogsForBot(botId)) {
            if ("ERROR".equals(entry.getLevel())) {
                String details = entry.getDetails() == null ? "" : entry.getDetails().trim();
                return details.isEmpty() ? entry.getMessage() : entry.getMessage() + ": " + details;
            }
        }
        return "";
    }

    private void confirmResetOffset() {
        if (bot == null) {
            return;
        }
        if (runnerManager.isRunning(bot.getId())) {
            UiUtils.showError(this, "Сначала остановите polling, затем сбросьте offset.");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Сбросить offset?")
                .setMessage("BotForge забудет last update id. При следующем запуске Telegram может вернуть старые непрочитанные updates.")
                .setPositiveButton("Сбросить", (dialog, which) -> {
                    botRepository.resetLastUpdateId(bot.getId());
                    logRepository.warn(bot, "Offset сброшен вручную", "");
                    UiUtils.toast(this, "Offset сброшен");
                    refreshBot();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void confirmDelete() {
        if (bot == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Удалить бота?")
                .setMessage("Бот, скрипт и его запуск будут остановлены. Логи останутся в журнале.")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    runnerManager.stopBot(bot.getId());
                    scriptRepository.deleteScript(bot.getScriptId());
                    botRepository.deleteBot(bot.getId());
                    UiUtils.toast(this, "Бот удалён");
                    finish();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
