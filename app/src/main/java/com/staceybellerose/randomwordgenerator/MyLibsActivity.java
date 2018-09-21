package com.staceybellerose.randomwordgenerator;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment;
import com.staceybellerose.randomwordgenerator.listeners.SpecialButtonListener;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Display the About Screen for this app, including a list of third party libraries used
 */
public class MyLibsActivity extends AppCompatActivity {
    /**
     * The toolbar
     */
    @BindView(R.id.toolbar) Toolbar mToolbar;
    /**
     * Changelog Activity title string
     */
    @BindString(R.string.title_activity_changelog) String mChangelogActivityTitle;
    /**
     * The app name
     */
    @BindString(R.string.app_name) String mAppName;
    /**
     * Long description for the app
     */
    @BindString(R.string.app_long_desc) String mAppLongDesc;
    /**
     * Title string for the Settings activity
     */
    @BindString(R.string.title_activity_settings) String mSettingsActivityTitle;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libraries);
        ButterKnife.bind(this);

        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

        LibsSupportFragment fragment = new LibsBuilder()
                .withFields(R.string.class.getFields())
                .withAutoDetect(true)
                .withAboutAppName(mAppName)
                .withAboutIconShown(true)
                .withAboutVersionShownName(true)
                .withAboutDescription(mAppLongDesc)
                .withAboutSpecial1(mSettingsActivityTitle)
                .withAboutSpecial2(mChangelogActivityTitle)
                .withListener(new SpecialButtonListener())
                .withExcludedLibraries("AndroidIconics")
                .withLibraries("12dicts", "gradle-retrolambda", "retrolambda", "rxandroid")
                .withLicenseShown(true)
                .withOwnLibsActivityClass(MyLibsActivity.class)
                .supportFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_container, fragment).commit();
    }
}
