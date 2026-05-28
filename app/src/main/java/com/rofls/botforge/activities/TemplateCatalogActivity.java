package com.rofls.botforge.activities;

import android.app.Activity;
import android.app.AlertDialog;
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

        templateRepository = new TemplateRepository(this);
        templatesContainer = findViewById(R.id.templatesContainer);
        Button buttonCreateTemplate = findViewById(R.id.buttonCreateTemplate);
        buttonCreateTemplate.setOnClickListener(v -> createTemplate());
        renderTemplates();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            LinearLayout customActions = card.findViewById(R.id.customTemplateActions);
            Button buttonEdit = card.findViewById(R.id.buttonEditTemplate);
            Button buttonDelete = card.findViewById(R.id.buttonDeleteTemplate);

            textName.setText(template.getName() + (template.isBuiltIn() ? "" : " · мой"));
            textDescription.setText(template.getDescription());
            textDifficulty.setText("Сложность: " + template.getDifficulty());
            buttonUse.setOnClickListener(v -> useTemplate(template));
            customActions.setVisibility(template.isBuiltIn() ? View.GONE : View.VISIBLE);
            buttonEdit.setOnClickListener(v -> editTemplate(template));
            buttonDelete.setOnClickListener(v -> confirmDeleteTemplate(template));

            templatesContainer.addView(card);
        }
    }

    private void useTemplate(BotTemplate template) {
        Intent intent = new Intent(this, AddBotActivity.class);
        intent.putExtra("mode", "TEMPLATE");
        intent.putExtra("template_id", template.getId());
        startActivity(intent);
    }

    private void editTemplate(BotTemplate template) {
        Intent intent = new Intent(this, TemplateEditorActivity.class);
        intent.putExtra("template_id", template.getId());
        startActivity(intent);
    }

    private void createTemplate() {
        startActivity(new Intent(this, TemplateEditorActivity.class));
    }

    private void confirmDeleteTemplate(BotTemplate template) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить шаблон?")
                .setMessage("Уже созданные боты не изменятся: у них есть собственные копии скрипта.")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    templateRepository.deleteCustomTemplate(template.getId());
                    renderTemplates();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
