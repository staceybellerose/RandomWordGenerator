package com.staceybellerose.randomwordgenerator.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.BoolRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringRes;

import com.staceybellerose.randomwordgenerator.BuildConfig;
import com.staceybellerose.randomwordgenerator.R;

/**
 * Utility singleton class to manage shared preferences.
 */
@SuppressLint("UseSparseArrays")
public final class Settings {
    /**
     * the singleton instance
     */
    private static Settings instance;
    /**
     * Shared preferences
     */
    private final SharedPreferences mPrefs;
    /**
     * Minimum word length to be displayed
     */
    private int mMinLength;
    /**
     * Maximum word length to be displayed
     */
    private int mMaxLength;
    /**
     * Name of word list to display
     */
    private String mWordListName;
    /**
     * Flag indicating whether to display only clean words
     */
    private boolean mCleanWordsOnly;
    /**
     * Number of words to display
     */
    private int mDisplayCount;
    /**
     * Flag indicating whether to display words in two columns
     */
    private boolean mDisplayTwoColumns;
    /**
     * A map to translate resource IDs to their values
     */
    private final TypedMap mResourceMap = new TypedMap();

    /**
     * Private Constructor
     *
     * @param context Any sort of Context
     */
    private Settings(final Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mResourceMap.put(R.string.pref_word_list, getString(context, R.string.pref_word_list));
        mResourceMap.put(R.string.pref_word_list_default, getString(context, R.string.pref_word_list_default));
        mResourceMap.put(R.string.pref_clean_words_flag, getString(context, R.string.pref_clean_words_flag));
        mResourceMap.put(R.string.pref_display_count, getString(context, R.string.pref_display_count));
        mResourceMap.put(R.string.pref_unlimited_length_flag, getString(context, R.string.pref_unlimited_length_flag));
        mResourceMap.put(R.string.pref_min_word_length, getString(context, R.string.pref_min_word_length));
        mResourceMap.put(R.string.pref_max_word_length, getString(context, R.string.pref_max_word_length));
        mResourceMap.put(R.integer.default_display_count, getInteger(context, R.integer.default_display_count));
        mResourceMap.put(R.integer.default_min_word_length, getInteger(context, R.integer.default_min_word_length));
        mResourceMap.put(R.integer.default_max_word_length, getInteger(context, R.integer.default_max_word_length));
        mResourceMap.put(R.bool.pref_two_column_default, getBoolean(context, R.bool.pref_two_column_default));
        refreshPreferences();
    }

    /**
     * Thread-safe method to get the instance of the Settings singleton
     *
     * @param context Any sort of Context
     * @return the Settings singleton
     */
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public static synchronized Settings getInstance(final Context context) {
        if (instance == null) {
            instance = new Settings(context);
        }
        return instance;
    }

    /**
     * Refresh the shared preference values.
     */
    public void refreshPreferences() {
        loadWordLengths();
        mWordListName = mPrefs.getString(mResourceMap.getString(R.string.pref_word_list),
                mResourceMap.getString(R.string.pref_word_list_default));
        mCleanWordsOnly = mPrefs.getBoolean(mResourceMap.getString(R.string.pref_clean_words_flag),
                BuildConfig.CLEAN_WORDS_ONLY);
        mDisplayCount = mPrefs.getInt(mResourceMap.getString(R.string.pref_display_count),
                mResourceMap.getInteger(R.integer.default_display_count));
        mDisplayTwoColumns = mPrefs.getBoolean(mResourceMap.getString(R.string.pref_two_column),
                mResourceMap.getBoolean(R.bool.pref_two_column_default));
    }

    /**
     * Get the min and max allowed word lengths from shared preferences.
     */
    private void loadWordLengths() {
        final boolean unlimitedSize
                = mPrefs.getBoolean(mResourceMap.getString(R.string.pref_unlimited_length_flag), true);
        final int defaultMinLength = mResourceMap.getInteger(R.integer.default_min_word_length);
        final int defaultMaxLength = mResourceMap.getInteger(R.integer.default_max_word_length);
        if (unlimitedSize) {
            mMinLength = defaultMinLength;
            mMaxLength = defaultMaxLength;
        } else {
            mMinLength = mPrefs.getInt(mResourceMap.getString(R.string.pref_min_word_length), defaultMinLength);
            mMaxLength = mPrefs.getInt(mResourceMap.getString(R.string.pref_max_word_length), defaultMaxLength);
            checkWordLengths();
        }
    }

    /**
     * Verify that the min and max word lengths haven't been swapped, and fix them if necessary.
     */
    private void checkWordLengths() {
        if (mMinLength > mMaxLength) {
            // swap values and store the corrected values in shared preferences
            final int temp = mMinLength;
            mMinLength = mMaxLength;
            mMaxLength = temp;
            final SharedPreferences.Editor editor = mPrefs.edit();
            editor.putInt(mResourceMap.getString(R.string.pref_min_word_length), mMinLength);
            editor.putInt(mResourceMap.getString(R.string.pref_max_word_length), mMaxLength);
            editor.apply();
        }
    }

    /**
     * Return the string value associated with a particular resource ID.  It
     * will be stripped of any styled text information.
     *
     * @param context The context, used to get the resource
     * @param resourceId The desired resource identifier, as generated by the aapt
     *                   tool. This integer encodes the package, type, and resource
     *                   entry. The value 0 is an invalid identifier.
     *
     * @return String The string data associated with the resource,
     *         stripped of styled text information.
     */
    private String getString(final Context context, @StringRes final int resourceId) {
        return context.getResources().getString(resourceId);
    }

    /**
     * Return an integer associated with a particular resource ID.
     *
     * @param context The context, used to get the resource
     * @param resourceId The desired resource identifier, as generated by the aapt
     *                   tool. This integer encodes the package, type, and resource
     *                   entry. The value 0 is an invalid identifier.
     *
     * @return Returns the integer value contained in the resource.
     */
    private int getInteger(final Context context, @IntegerRes final int resourceId) {
        return context.getResources().getInteger(resourceId);
    }

    /**
     * Return a boolean associated with a particular resource ID.  This can be
     * used with any integral resource value, and will return true if it is
     * non-zero.
     *
     * @param context The context, used to get the resource
     * @param resourceId The desired resource identifier, as generated by the aapt
     *                   tool. This integer encodes the package, type, and resource
     *                   entry. The value 0 is an invalid identifier.
     *
     * @return Returns the boolean value contained in the resource.
     */
    private boolean getBoolean(final Context context, @BoolRes final int resourceId) {
        return context.getResources().getBoolean(resourceId);
    }

    public int getMinLength() {
        return mMinLength;
    }

    public int getMaxLength() {
        return mMaxLength;
    }

    public String getWordListName() {
        return mWordListName;
    }

    public int getDisplayCount() {
        return mDisplayCount;
    }

    public boolean isCleanWordsOnly() {
        return mCleanWordsOnly;
    }

    public boolean isDisplayTwoColumns() {
        return mDisplayTwoColumns;
    }
}
