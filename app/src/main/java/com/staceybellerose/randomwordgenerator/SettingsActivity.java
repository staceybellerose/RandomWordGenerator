package com.staceybellerose.randomwordgenerator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Manage the Settings for this app
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_container, new SettingsFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
