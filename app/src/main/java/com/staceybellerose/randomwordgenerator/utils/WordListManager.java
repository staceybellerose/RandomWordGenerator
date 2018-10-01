package com.staceybellerose.randomwordgenerator.utils;

import android.content.Context;
import android.support.annotation.RawRes;
import android.text.TextUtils;
import android.util.Log;

import com.github.davidmoten.rx2.Strings;
import com.staceybellerose.randomwordgenerator.BuildConfig;
import com.staceybellerose.randomwordgenerator.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

/**
 * Manage internal word lists
 */
public class WordListManager {
    /**
     * tag string for error logging
     */
    private static final String TAG = "WordListManager";
    /**
     * Time in milliseconds to delay between new words displaying
     */
    private static final int TIME_BETWEEN_WORD_DISPLAY_MS = 100;
    /**
     * Utility object to manage reading our shared preferences
     */
    private final Settings mSettings;
    /**
     * The activity context
     */
    private final Context mContext;
    /**
     * The word list to use in generating randomly selected words.
     */
    private final List<String> mWordList = new ArrayList<>();
    /**
     * The list of slur words to filter from the final selection.
     */
    private final List<String> mFilterList = new ArrayList<>();
    /**
     * The list of vulgar words to filter from the final selection.
     */
    private final List<String> mCleanFilter = new ArrayList<>();
    /**
     * cryptographically secure random number generator
     */
    private final SecureRandom mSecureRandom = new SecureRandom();

    /**
     * Constructor
     *
     * @param context the activity context
     */
    public WordListManager(final Context context) {
        mContext = context;
        mSettings = Settings.getInstance(context);
        mFilterList.addAll(readWordList(R.raw.filter_slurs, 0, Integer.MAX_VALUE));
    }

    /**
     * Reload the word list based on the one set in shared preferences.
     *
     * @return the word list name
     */
    public String reloadWordList() {
        int wordListId;
        final String wordListName = mSettings.getWordListName();
        wordListId = mContext.getResources().getIdentifier(wordListName, "raw", mContext.getPackageName());
        if (wordListId == 0) {
            // something bad happened and we don't have a proper resource name; default to original word list
            wordListId = R.raw.wordlist;
        }
        mWordList.clear();
        mWordList.addAll(readWordList(wordListId, mSettings.getMinLength(), mSettings.getMaxLength()));
        return wordListName;
    }

    /**
     * Reload the clean words filter based on shared preferences setting.
     */
    public void reloadCleanFilter() {
        mCleanFilter.clear();
        if (BuildConfig.CLEAN_WORDS_ONLY || mSettings.isCleanWordsOnly()) {
            mCleanFilter.addAll(readWordList(R.raw.filter_clean, 0, Integer.MAX_VALUE));
        }
    }

    /**
     * Read the list of words from a raw resource file.
     *
     * @param wordListResource A raw resource containing the word list to process
     * @param minLength        The minimum word length
     * @param maxLength        The maximum word length
     * @return The list of words present in the word list
     */
    private List<String> readWordList(@RawRes final int wordListResource, final int minLength,
                                      final int maxLength) {
        final ArrayList<String> wordList = new ArrayList<>();
        final InputStream inputStream = mContext.getResources().openRawResource(wordListResource);
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(inputStream, "UTF8");
        } catch (UnsupportedEncodingException exception) {
            Log.e(TAG, exception.getMessage(), exception);
        }
        Strings.split(Strings.from(reader), "\n")
                .filter(substring -> !TextUtils.isEmpty(substring))
                .map(substring -> substring.split("\t")[0])
                .filter(substring -> substring.length() >= minLength)
                .filter(substring -> substring.length() <= maxLength)
                .subscribe(wordList::add);
        try {
            if (reader != null) {
                reader.close();
            }
            inputStream.close();
        } catch (IOException exception) {
            Log.e(TAG, exception.getMessage(), exception);
        }
        return wordList;
    }

    /**
     * Refresh the list of random words.
     * @return a Flowable with the randomly selected words
     */
    public Flowable<String> refreshWords() {
        return Flowable.interval(TIME_BETWEEN_WORD_DISPLAY_MS, TimeUnit.MILLISECONDS, Schedulers.io())
                .map(number -> mSecureRandom.nextInt(mWordList.size()))
                .map(mWordList::get)
                .filter(substring -> substring != null)
                .filter(substring -> mFilterList.indexOf(substring) == -1)
                .filter(substring -> (mCleanFilter == null) || mCleanFilter.indexOf(substring) == -1)
                .distinct()
                .take(mSettings.getDisplayCount());
    }
}
