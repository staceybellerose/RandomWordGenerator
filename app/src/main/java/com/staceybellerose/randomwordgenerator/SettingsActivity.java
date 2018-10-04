package com.staceybellerose.randomwordgenerator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.staceybellerose.randomwordgenerator.utils.Settings;
import com.staceybellerose.randomwordgenerator.utils.WordListManager;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Manage the Settings for this app
 */
public class SettingsActivity extends AppCompatActivity implements SettingsFragment.OnPreferenceChangeListener {
    /**
     * Request code to indicate Settings Activity called
     */
    public static final int SETTINGS_REQUEST_CODE = 42;
    /**
     * the toolbar
     */
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    /**
     * The result intent to be used when this activity called from startActivityForResult
     */
    private final Intent mResultIntent = new Intent();
    /**
     * Utility object to manage reading our shared preferences
     */
    private Settings mSettings;
    /**
     * Word list manager
     */
    private WordListManager mWordListManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        mSettings = Settings.getInstance(this);
        mWordListManager = new WordListManager(this);

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

    @Override
    public Settings getSettings() {
        return mSettings;
    }

    @Override
    public WordListManager getWordListManager() {
        return mWordListManager;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == WordListDetailsActivity.WORD_LIST_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data.getBooleanExtra(getString(R.string.pref_word_list_changed), false)) {
                onWordListChanged();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
