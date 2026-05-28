package com.rofls.botforge.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rofls.botforge.R;
import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.LogEntry;
import com.rofls.botforge.repository.BotRepository;
import com.rofls.botforge.repository.LogRepository;
import com.rofls.botforge.utils.DateUtils;
import com.rofls.botforge.utils.UiUtils;

import java.util.List;

public class LogsActivity extends Activity {
    private static final long AUTO_REFRESH_MS = 3000L;

    private String botId;
    private LogRepository logRepository;
    private BotRepository botRepository;
    private LinearLayout logsContainer;
    private TextView textLogsTitle;
    private TextView textLogsEmpty;
    private Handler handler;
    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            renderLogs();
            handler.postDelayed(this, AUTO_REFRESH_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        botId = getIntent().getStringExtra("bot_id");
        logRepository = new LogRepository(this);
        botRepository = new BotRepository(this);
        handler = new Handler(Looper.getMainLooper());
        logsContainer = findViewById(R.id.logsContainer);
        textLogsTitle = findViewById(R.id.textLogsTitle);
        textLogsEmpty = findViewById(R.id.textLogsEmpty);
        Button buttonClearLogs = findViewById(R.id.buttonClearLogs);
        Button buttonRefreshLogs = findViewById(R.id.buttonRefreshLogs);

        buttonClearLogs.setOnClickListener(v -> {
            if (botId == null || botId.isEmpty()) {
                logRepository.clearAll();
            } else {
                logRepository.clearForBot(botId);
            }
            UiUtils.toast(this, "Логи очищены");
            renderLogs();
        });
        buttonRefreshLogs.setOnClickListener(v -> renderLogs());

        renderLogs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderLogs();
        handler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(autoRefreshRunnable);
    }

    private void renderLogs() {
        List<LogEntry> logs;
        if (botId == null || botId.isEmpty()) {
            logs = logRepository.getAllLogs();
            textLogsTitle.setText("Журнал");
        } else {
            logs = logRepository.getLogsForBot(botId);
            Bot bot = botRepository.getBot(botId);
            textLogsTitle.setText(bot == null ? "Журнал бота" : "Журнал: " + bot.getName());
        }

        logsContainer.removeAllViews();
        textLogsEmpty.setVisibility(logs.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (LogEntry entry : logs) {
            View card = inflater.inflate(R.layout.item_log_entry, logsContainer, false);
            TextView header = card.findViewById(R.id.textLogHeader);
            TextView message = card.findViewById(R.id.textLogMessage);
            TextView details = card.findViewById(R.id.textLogDetails);

            header.setText(DateUtils.format(entry.getTimestamp())
                    + " · " + levelText(entry.getLevel())
                    + " · " + entry.getBotName());
            header.setTextColor(colorForLevel(entry.getLevel()));
            message.setText(entry.getMessage());
            if (entry.getDetails() != null && !entry.getDetails().trim().isEmpty()) {
                details.setText(entry.getDetails());
                details.setVisibility(View.VISIBLE);
            } else {
                details.setVisibility(View.GONE);
            }
            logsContainer.addView(card);
        }
    }

    private int colorForLevel(String level) {
        if ("ERROR".equals(level)) {
            return getColor(R.color.bf_error);
        }
        if ("WARN".equals(level)) {
            return getColor(R.color.bf_warning);
        }
        return getColor(R.color.bf_text_secondary);
    }

    private String levelText(String level) {
        if ("ERROR".equals(level)) {
            return "ОШИБКА";
        }
        if ("WARN".equals(level)) {
            return "ПРЕДУПРЕЖДЕНИЕ";
        }
        return "ИНФО";
    }
}
