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
import com.rofls.botforge.models.BotTemplate;
import com.rofls.botforge.repository.TemplateRepository;

public class TemplateCatalogActivity extends Activity {
    private LinearLayout templatesContainer;
    private TemplateRepository templateRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_catalog);

        templateRepository = new TemplateRepository();
        templatesContainer = findViewById(R.id.templatesContainer);
        renderTemplates();
    }

    private void renderTemplates() {
        templatesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (BotTemplate template : templateRepository.getTemplates()) {
            View card = inflater.inflate(R.layout.item_template_card, templatesContainer, false);
            TextView textName = card.findViewById(R.id.textTemplateName);
            TextView textDescription = card.findViewById(R.id.textTemplateDescription);
            TextView textDifficulty = card.findViewById(R.id.textTemplateDifficulty);
            Button buttonUse = card.findViewById(R.id.buttonUseTemplate);

            textName.setText(template.getName());
            textDescription.setText(template.getDescription());
            textDifficulty.setText("Сложность: " + template.getDifficulty());
            buttonUse.setOnClickListener(v -> useTemplate(template));

            templatesContainer.addView(card);
        }
    }

    private void useTemplate(BotTemplate template) {
        Intent intent = new Intent(this, AddBotActivity.class);
        intent.putExtra("mode", "TEMPLATE");
        intent.putExtra("template_id", template.getId());
        startActivity(intent);
    }
}
