package com.rofls.botforge.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rofls.botforge.R;
import com.rofls.botforge.models.BotTemplate;
import com.rofls.botforge.repository.TemplateRepository;
import com.rofls.botforge.utils.UiUtils;

public class TemplateEditorActivity extends Activity {
    private TemplateRepository templateRepository;
    private String templateId;
    private BotTemplate editingTemplate;

    private TextView textTitle;
    private EditText inputName;
    private EditText inputDescription;
    private EditText inputDifficulty;
    private EditText inputScript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_editor);

        templateRepository = new TemplateRepository(this);
        templateId = getIntent().getStringExtra("template_id");

        textTitle = findViewById(R.id.textTemplateEditorTitle);
        inputName = findViewById(R.id.inputTemplateName);
        inputDescription = findViewById(R.id.inputTemplateDescription);
        inputDifficulty = findViewById(R.id.inputTemplateDifficulty);
        inputScript = findViewById(R.id.inputTemplateScript);
        Button buttonSave = findViewById(R.id.buttonSaveTemplate);

        loadTemplate();
        buttonSave.setOnClickListener(v -> saveTemplate());
    }

    private void loadTemplate() {
        if (templateId != null && TemplateRepository.isCustomTemplateId(templateId)) {
            editingTemplate = templateRepository.findTemplate(templateId);
        }

        if (editingTemplate != null) {
            textTitle.setText("Редактировать шаблон");
            inputName.setText(editingTemplate.getName());
            inputDescription.setText(editingTemplate.getDescription());
            inputDifficulty.setText(editingTemplate.getDifficulty());
            inputScript.setText(editingTemplate.getDefaultScript());
            return;
        }

        textTitle.setText("Новый шаблон");
        inputName.setText(getIntent().getStringExtra("source_name"));
        inputDescription.setText(getIntent().getStringExtra("source_description"));
        inputDifficulty.setText("Пользовательский");
        String sourceScript = getIntent().getStringExtra("source_script");
        inputScript.setText(sourceScript == null ? starterScript() : sourceScript);
    }

    private void saveTemplate() {
        String name = inputName.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();
        String difficulty = inputDifficulty.getText().toString().trim();
        String script = inputScript.getText().toString();

        if (name.isEmpty()) {
            UiUtils.showError(this, "Введите название шаблона.");
            return;
        }
        if (description.isEmpty()) {
            UiUtils.showError(this, "Введите описание шаблона.");
            return;
        }
        if (difficulty.isEmpty()) {
            UiUtils.showError(this, "Введите сложность шаблона.");
            return;
        }
        if (!isBasicScriptValid(script)) {
            UiUtils.showError(this, "Код шаблона должен содержать @bot.command, @bot.message или def.");
            return;
        }

        BotTemplate template = editingTemplate == null
                ? new BotTemplate("", name, description, difficulty, script, false, 0L, 0L)
                : editingTemplate;
        template.setName(name);
        template.setDescription(description);
        template.setDifficulty(difficulty);
        template.setDefaultScript(script);

        templateRepository.saveCustomTemplate(template);
        UiUtils.toast(this, "Шаблон сохранён");
        finish();
    }

    private boolean isBasicScriptValid(String code) {
        String trimmed = code == null ? "" : code.trim();
        return !trimmed.isEmpty()
                && (trimmed.contains("@bot.command")
                || trimmed.contains("@bot.message")
                || trimmed.contains("def "));
    }

    private String starterScript() {
        return "from botforge import bot\n\n"
                + "@bot.command(\"/start\")\n"
                + "def start(ctx):\n"
                + "    ctx.reply(\"Привет! Это мой пользовательский шаблон.\")\n\n"
                + "@bot.message()\n"
                + "def echo(ctx):\n"
                + "    ctx.reply(\"Вы написали: \" + ctx.text)\n";
    }
}
