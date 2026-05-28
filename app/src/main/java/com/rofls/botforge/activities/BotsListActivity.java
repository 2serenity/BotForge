package com.rofls.botforge.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rofls.botforge.R;
import com.rofls.botforge.models.Bot;
import com.rofls.botforge.models.BotStatus;
import com.rofls.botforge.models.BotTemplate;
import com.rofls.botforge.repository.BotRepository;
import com.rofls.botforge.repository.TemplateRepository;
import com.rofls.botforge.runner.BotRunnerManager;

import java.util.List;

public class BotsListActivity extends Activity {
    private BotRepository botRepository;
    private BotRunnerManager runnerManager;
    private LinearLayout botsContainer;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bots_list);

        botRepository = new BotRepository(this);
        runnerManager = BotRunnerManager.getInstance(this);
        botsContainer = findViewById(R.id.botsContainer);
        textEmpty = findViewById(R.id.textEmpty);

        Button buttonAddBot = findViewById(R.id.buttonAddBot);
        Button buttonTemplates = findViewById(R.id.buttonTemplates);
        Button buttonLogs = findViewById(R.id.buttonLogs);
        Button buttonSettings = findViewById(R.id.buttonSettings);

        buttonAddBot.setOnClickListener(v -> startActivity(new Intent(this, AddBotActivity.class)));
        buttonTemplates.setOnClickListener(v -> startActivity(new Intent(this, TemplateCatalogActivity.class)));
        buttonLogs.setOnClickListener(v -> startActivity(new Intent(this, LogsActivity.class)));
        buttonSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderBots();
    }

    private void renderBots() {
        botsContainer.removeAllViews();
        List<Bot> bots = botRepository.getAllBots();
        textEmpty.setVisibility(bots.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Bot bot : bots) {
            syncRuntimeStatus(bot);
            View card = inflater.inflate(R.layout.item_bot_card, botsContainer, false);
            TextView textBotName = card.findViewById(R.id.textBotName);
            TextView textBotMeta = card.findViewById(R.id.textBotMeta);
            TextView textBotStatus = card.findViewById(R.id.textBotStatus);

            BotTemplate template = bot.getTemplateId() == null || bot.getTemplateId().isEmpty()
                    ? null
                    : new TemplateRepository(this).findTemplate(bot.getTemplateId());
            String templateName = template == null
                    ? (TemplateRepository.isCustomTemplateId(bot.getTemplateId())
                    ? "Пользовательский шаблон (удалён)"
                    : "Developer Mode")
                    : template.getName();

            textBotName.setText(bot.getName());
            textBotMeta.setText("@" + bot.getUsername() + " · " + bot.getMode().name() + " · " + templateName);
            textBotStatus.setText(statusText(bot.getStatus()));
            textBotStatus.setTextColor(statusColor(bot.getStatus()));

            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, BotDetailActivity.class);
                intent.putExtra("bot_id", bot.getId());
                startActivity(intent);
            });
            botsContainer.addView(card);
        }
    }

    private void syncRuntimeStatus(Bot bot) {
        if (runnerManager.isRunning(bot.getId())) {
            if (bot.getStatus() != BotStatus.RUNNING) {
                bot.setStatus(BotStatus.RUNNING);
                botRepository.saveBot(bot);
            }
            return;
        }
        if (bot.getStatus() == BotStatus.RUNNING) {
            bot.setStatus(BotStatus.STOPPED);
            botRepository.saveBot(bot);
        }
    }

    private String statusText(BotStatus status) {
        if (status == BotStatus.RUNNING) {
            return "RUNNING";
        }
        if (status == BotStatus.ERROR) {
            return "ERROR";
        }
        return "STOPPED";
    }

    private int statusColor(BotStatus status) {
        if (status == BotStatus.RUNNING) {
            return getColor(R.color.bf_success);
        }
        if (status == BotStatus.ERROR) {
            return getColor(R.color.bf_error);
        }
        return getColor(R.color.bf_text_secondary);
    }
}
