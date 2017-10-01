package com.staceybellerose.randomwordgenerator;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * A Fragment to display the Settings
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
