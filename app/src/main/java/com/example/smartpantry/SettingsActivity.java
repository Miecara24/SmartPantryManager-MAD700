package com.example.smartpantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS = "smart_pantry_settings";
    private static final String EXPIRY = "expiry_alerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch toggle = findViewById(R.id.switchExpiry);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        toggle.setChecked(prefs.getBoolean(EXPIRY, true));
        toggle.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(EXPIRY, isChecked).apply());
    }
}
