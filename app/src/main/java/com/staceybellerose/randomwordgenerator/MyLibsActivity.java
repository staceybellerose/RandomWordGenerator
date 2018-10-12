package com.staceybellerose.randomwordgenerator;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment;

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
     * The app name
     */
    @BindString(R.string.app_name) String mAppName;
    /**
     * Long description for the app
     */
    @BindString(R.string.app_long_desc) String mAppLongDesc;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libraries);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        final LibsSupportFragment fragment = new LibsBuilder()
                .withFields(R.string.class.getFields())
                .withAutoDetect(true)
                .withAboutAppName(mAppName)
                .withAboutIconShown(true)
                .withAboutVersionShownName(true)
                .withAboutDescription(mAppLongDesc)
                .withExcludedLibraries("AndroidIconics")
                .withLibraries("gradle-retrolambda", "retrolambda", "rxandroid")
                .withLicenseShown(true)
                .withOwnLibsActivityClass(MyLibsActivity.class)
                .supportFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_container, fragment).commit();
    }
}
