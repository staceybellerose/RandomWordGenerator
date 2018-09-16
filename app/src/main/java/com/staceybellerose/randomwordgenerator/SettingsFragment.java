package com.staceybellerose.randomwordgenerator;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

/**
 * A Fragment to display the Settings
 */
public class SettingsFragment extends PreferenceFragment {
    /**
     * The calling activity, implementing the notification interface.
     */
    private OnPreferenceChangeListener mCallback;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference wordList = findPreference(getString(R.string.pref_word_list));
        wordList.setOnPreferenceChangeListener((preference, newValue) -> {
            mCallback.onWordListChanged();
            return true;
        });
        Preference cleanWordsPreference = findPreference(getString(R.string.pref_clean_words_flag));
        if (BuildConfig.CLEAN_WORDS_ONLY) {
            PreferenceCategory preferenceCategory
                    = (PreferenceCategory) findPreference(getString(R.string.pref_filters_header));
            preferenceCategory.removePreference(cleanWordsPreference);
        } else {
            cleanWordsPreference.setOnPreferenceChangeListener(((preference, newValue) -> {
                mCallback.onCleanWordsChanged();
                return true;
            }));
        }
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnPreferenceChangeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnWordListChangedListener");
        }
    }

    /**
     * This interface is to be implemented by any activity using this fragment, for notifications when
     * shared preferences change.
     */
    interface OnPreferenceChangeListener {
        /**
         * This method is called when the selected word list is changed.
         */
        void onWordListChanged();

        /**
         * This method is called when the shared preference for clean words is changed.
         */
        void onCleanWordsChanged();
    }
}
