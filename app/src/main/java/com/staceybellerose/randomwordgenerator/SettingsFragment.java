package com.staceybellerose.randomwordgenerator;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

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
        Preference.OnPreferenceChangeListener wordListChangeListener = (preference, newValue) -> {
            mCallback.onWordListChanged();
            return true;
        };

        wordList.setOnPreferenceChangeListener(wordListChangeListener);
        Preference unrestricted = findPreference(getString(R.string.pref_unlimited_length_flag));
        unrestricted.setOnPreferenceChangeListener(wordListChangeListener);
        Preference maxLength = findPreference(getString(R.string.pref_max_word_length));
        maxLength.setOnPreferenceChangeListener(wordListChangeListener);
        Preference minLength = findPreference(getString(R.string.pref_min_word_length));
        minLength.setOnPreferenceChangeListener(wordListChangeListener);

        Preference cleanWordsPreference = findPreference(getString(R.string.pref_clean_words_flag));
        if (BuildConfig.CLEAN_WORDS_ONLY) {
            PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference(getString(R.string.pref_filters_header));
            preferenceGroup.removePreference(cleanWordsPreference);
        } else {
            cleanWordsPreference.setOnPreferenceChangeListener(((preference, newValue) -> {
                mCallback.onCleanWordsChanged();
                return true;
            }));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        setCallback(activity);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        setCallback(context);
    }

    /**
     * Set up the calling activity as a callback implementation.
     *
     * @param context The activity that is attaching this fragment
     */
    private void setCallback(final Context context) {
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
