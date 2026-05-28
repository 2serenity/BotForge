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
    private static final int MAX_TRANSIENT_FAILURES = 6;
    private static final long INITIAL_RETRY_DELAY_MS = 2000L;
    private static final long MAX_RETRY_DELAY_MS = 60000L;

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
        logRepository.info(bot, "Опрос запущен");

        long offset = bot.getLastUpdateId() > 0L ? bot.getLastUpdateId() + 1L : 0L;
        int transientFailures = 0;

        while (running.get()) {
            try {
                List<TelegramUpdate> updates = apiClient.getUpdates(bot.getToken(), offset);
                if (transientFailures > 0) {
                    logRepository.info(bot, "Опрос восстановлен после сетевой ошибки");
                    transientFailures = 0;
                }
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
                            logRepository.info(bot, "Ответ отправлен"
                                    + (response.getText().length() > TelegramApiClient.MAX_MESSAGE_LENGTH
                                    ? " несколькими сообщениями"
                                    : ""));
                        }
                    }

                    if (update.getUpdateId() > 0L) {
                        bot.setLastUpdateId(update.getUpdateId());
                        botRepository.updateLastUpdateId(bot.getId(), update.getUpdateId());
                        offset = update.getUpdateId() + 1L;
                    }
                }
            } catch (TelegramApiClient.TelegramApiException ex) {
                if (running.get()) {
                    failed = true;
                    running.set(false);
                    botRepository.updateStatus(bot.getId(), BotStatus.ERROR);
                    logRepository.error(bot, "Опрос остановлен из-за ошибки Telegram API", ex.getMessage());
                }
            } catch (Exception ex) {
                if (running.get()) {
                    transientFailures++;
                    if (transientFailures > MAX_TRANSIENT_FAILURES) {
                        failed = true;
                        running.set(false);
                        botRepository.updateStatus(bot.getId(), BotStatus.ERROR);
                        logRepository.error(bot, "Опрос остановлен после повторных сетевых ошибок", ex.getMessage());
                    } else {
                        long delay = retryDelayMs(transientFailures);
                        logRepository.warn(bot,
                                "Временная ошибка опроса, повтор через " + (delay / 1000L) + " сек.",
                                ex.getMessage());
                        sleepBeforeRetry(delay);
                    }
                }
            }
        }

        if (!failed) {
            botRepository.updateStatus(bot.getId(), BotStatus.STOPPED);
            logRepository.info(bot, "Опрос остановлен");
        }
        running.set(false);
    }

    private long retryDelayMs(int failureCount) {
        long delay = INITIAL_RETRY_DELAY_MS;
        for (int i = 1; i < failureCount; i++) {
            delay *= 2L;
            if (delay >= MAX_RETRY_DELAY_MS) {
                return MAX_RETRY_DELAY_MS;
            }
        }
        return delay;
    }

    private void sleepBeforeRetry(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
