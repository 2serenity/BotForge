package com.rofls.botforge.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.rofls.botforge.R;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button buttonScriptApiDocs = findViewById(R.id.buttonScriptApiDocs);
        buttonScriptApiDocs.setOnClickListener(v ->
                startActivity(new Intent(this, ScriptApiDocsActivity.class))
        );
    }
}
