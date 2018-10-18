package com.staceybellerose.randomwordgenerator;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 */
public class WordListSourceActivity extends AppCompatActivity {
    /**
     * The toolbar
     */
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sources);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        final LibsSupportFragment fragment = getAboutFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_container, fragment).commit();
    }

    /**
     * get the About fragment
     *
     * @return the About fragment
     */
    private LibsSupportFragment getAboutFragment() {
        return new LibsBuilder()
                .withAutoDetect(false)
                .withLibraries("12dicts", "nltk", "europarl", "oanc", "bnc")
                .withExcludedLibraries("AndroidIconics", "AboutLibraries", "fastadapter")
                .withLicenseShown(true)
                .withOwnLibsActivityClass(WordListSourceActivity.class)
                .supportFragment();
    }
}
