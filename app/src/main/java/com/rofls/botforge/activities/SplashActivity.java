package com.rofls.botforge.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.rofls.botforge.R;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Button buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(v -> {
            startActivity(new Intent(this, BotsListActivity.class));
            finish();
        });
    }
}
