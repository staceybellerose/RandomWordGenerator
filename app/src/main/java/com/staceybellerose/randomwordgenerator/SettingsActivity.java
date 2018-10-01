package com.staceybellerose.randomwordgenerator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Manage the Settings for this app
 */
public class SettingsActivity extends AppCompatActivity implements SettingsFragment.OnPreferenceChangeListener {

    /**
     * the toolbar
     */
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    /**
     * The result intent to be used when this activity called from startActivityForResult
     */
    private final Intent mResultIntent = new Intent();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

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
