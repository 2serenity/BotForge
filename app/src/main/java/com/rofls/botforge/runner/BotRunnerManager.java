package com.rofls.botforge.runner;

import android.content.Context;

import com.rofls.botforge.engine.BotEngine;
import com.rofls.botforge.engine.PythonBotEngine;
import com.rofls.botforge.engine.TemplateBotEngine;
import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotMode;
import com.rofls.botforge.models.BotStatus;
import com.rofls.botforge.repository.BotRepository;
import com.rofls.botforge.repository.LogRepository;
import com.rofls.botforge.repository.ScriptRepository;
import com.rofls.botforge.repository.TemplateRepository;
import com.rofls.botforge.telegram.TelegramApiClient;

public class BotRunnerManager {
    private static BotRunnerManager instance;

    private final Context appContext;
    private final BotRepository botRepository;
    private final ScriptRepository scriptRepository;
    private final LogRepository logRepository;
    private final TelegramApiClient apiClient;

    private BotRunner activeRunner;

    private BotRunnerManager(Context context) {
        appContext = context.getApplicationContext();
        botRepository = new BotRepository(appContext);
        scriptRepository = new ScriptRepository(appContext);
        logRepository = new LogRepository(appContext);
        apiClient = new TelegramApiClient();
    }

    public static synchronized BotRunnerManager getInstance(Context context) {
        if (instance == null) {
            instance = new BotRunnerManager(context);
        }
        return instance;
    }

    public synchronized void startBot(Bot bot) throws IllegalStateException {
        cleanupStoppedRunner();
        if (activeRunner != null && activeRunner.isRunning()) {
            if (activeRunner.getBotId().equals(bot.getId())) {
                throw new IllegalStateException("Этот бот уже запущен.");
            }
            throw new IllegalStateException("В первой версии BotForge одновременно запускается один бот.");
        }

        boolean usesPython = bot.getMode() == BotMode.DEVELOPER
                || TemplateRepository.isCustomTemplateId(bot.getTemplateId());

        BotEngine engine = usesPython
                ? new PythonBotEngine(appContext, scriptRepository, logRepository)
                : new TemplateBotEngine(appContext);

        activeRunner = new BotRunner(bot, apiClient, engine, botRepository, logRepository);
        activeRunner.start();
    }

    public synchronized void stopBot(String botId) {
        if (activeRunner != null && activeRunner.isRunning() && activeRunner.getBotId().equals(botId)) {
            activeRunner.stop();
            botRepository.updateStatus(botId, BotStatus.STOPPED);
        }
        cleanupStoppedRunner();
    }

    public synchronized boolean isRunning(String botId) {
        cleanupStoppedRunner();
        return activeRunner != null && activeRunner.isRunning() && activeRunner.getBotId().equals(botId);
    }

    public synchronized String getActiveBotId() {
        cleanupStoppedRunner();
        return activeRunner == null ? "" : activeRunner.getBotId();
    }

    private void cleanupStoppedRunner() {
        if (activeRunner != null && !activeRunner.isRunning()) {
            activeRunner = null;
        }
    }
}
