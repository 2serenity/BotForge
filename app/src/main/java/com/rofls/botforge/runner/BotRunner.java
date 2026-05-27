package com.rofls.botforge.runner;

import com.rofls.botforge.engine.BotEngine;
import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotResponse;
import com.rofls.botforge.models.BotStatus;
import com.rofls.botforge.repository.BotRepository;
import com.rofls.botforge.repository.LogRepository;
import com.rofls.botforge.telegram.TelegramApiClient;
import com.rofls.botforge.telegram.TelegramMessage;
import com.rofls.botforge.telegram.TelegramUpdate;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BotRunner implements Runnable {
    private final Bot bot;
    private final TelegramApiClient apiClient;
    private final BotEngine engine;
    private final BotRepository botRepository;
    private final LogRepository logRepository;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private Thread thread;

    public BotRunner(
            Bot bot,
            TelegramApiClient apiClient,
            BotEngine engine,
            BotRepository botRepository,
            LogRepository logRepository
    ) {
        this.bot = bot;
        this.apiClient = apiClient;
        this.engine = engine;
        this.botRepository = botRepository;
        this.logRepository = logRepository;
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        thread = new Thread(this, "BotRunner-" + bot.getId());
        thread.start();
    }

    public void stop() {
        running.set(false);
        if (thread != null) {
            thread.interrupt();
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public String getBotId() {
        return bot.getId();
    }

    @Override
    public void run() {
        boolean failed = false;
        botRepository.markStarted(bot.getId());
        bot.setStatus(BotStatus.RUNNING);
        bot.setLastStartedAt(System.currentTimeMillis());
        logRepository.info(bot, "Polling запущен");

        long offset = bot.getLastUpdateId() > 0L ? bot.getLastUpdateId() + 1L : 0L;

        while (running.get()) {
            try {
                List<TelegramUpdate> updates = apiClient.getUpdates(bot.getToken(), offset);
                for (TelegramUpdate update : updates) {
                    if (!running.get()) {
                        break;
                    }
                    TelegramMessage message = update.getMessage();
                    if (message != null && message.getChatId() != 0L) {
                        logRepository.info(bot, "Получено сообщение: " + message.getText());
                        BotResponse response = engine.handleMessage(bot, message);
                        if (response != null && response.hasText()) {
                            apiClient.sendMessage(
                                    bot.getToken(),
                                    message.getChatId(),
                                    response.getText(),
                                    response.getButtons()
                            );
                            logRepository.info(bot, "Ответ отправлен");
                        }
                    }

                    if (update.getUpdateId() > 0L) {
                        bot.setLastUpdateId(update.getUpdateId());
                        botRepository.updateLastUpdateId(bot.getId(), update.getUpdateId());
                        offset = update.getUpdateId() + 1L;
                    }
                }
            } catch (Exception ex) {
                if (running.get()) {
                    failed = true;
                    running.set(false);
                    botRepository.updateStatus(bot.getId(), BotStatus.ERROR);
                    logRepository.error(bot, "Polling остановлен из-за ошибки", ex.getMessage());
                }
            }
        }

        if (!failed) {
            botRepository.updateStatus(bot.getId(), BotStatus.STOPPED);
            logRepository.info(bot, "Polling остановлен");
        }
        running.set(false);
    }
}
