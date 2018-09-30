package com.staceybellerose.randomwordgenerator;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import com.staceybellerose.randomwordgenerator.utils.Settings;

/**
 * A Fragment to display the Settings
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * The calling activity, implementing the notification interface.
     */
    private OnPreferenceChangeListener mCallback;
    /**
     * Utility object to manage reading our shared preferences
     */
    private Settings mSettings;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mSettings = Settings.getInstance(getActivity());

        Preference cleanWordsPreference = findPreference(getString(R.string.pref_clean_words_flag));
        if (BuildConfig.CLEAN_WORDS_ONLY) {
            PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference(getString(R.string.pref_filters_header));
            preferenceGroup.removePreference(cleanWordsPreference);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when a shared preference is changed, added, or removed. This may be called even if
     * a preference is set to its existing value.
     *
     * @param sharedPreferences The SharedPreferences that received the change.
     * @param key The key of the preference that was changed, added, or removed.
     */
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (key.equals(getString(R.string.pref_unlimited_length_flag))) {
            mCallback.onWordListChanged();
        } else if (key.equals(getString(R.string.pref_max_word_length))) {
            mCallback.onWordListChanged();
        } else if (key.equals(getString(R.string.pref_min_word_length))) {
            mCallback.onWordListChanged();
        } else if (key.equals(getString(R.string.pref_word_list))) {
            mCallback.onWordListChanged();
        } else if (key.equals(getString(R.string.pref_clean_words_flag))) {
            mCallback.onCleanWordsChanged();
        }
        mSettings.refreshPreferences();
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
