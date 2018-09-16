package com.staceybellerose.randomwordgenerator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Manage the Settings for this app
 */
public class SettingsActivity extends AppCompatActivity implements SettingsFragment.OnPreferenceChangeListener {

    /**
     * The result intent to be used when this activity called from startActivityForResult
     */
    private Intent mResultIntent = new Intent();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_container, new SettingsFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, mResultIntent);
        super.onBackPressed();
    }

    @Override
    public void onWordListChanged() {
        mResultIntent.putExtra(getString(R.string.pref_word_list_changed), true);
    }

    @Override
    public void onCleanWordsChanged() {
        mResultIntent.putExtra(getString(R.string.pref_clean_filter_changed), true);
    }
}
