package com.staceybellerose.randomwordgenerator.utils;

import android.content.Context;
import android.support.annotation.RawRes;
import android.text.TextUtils;
import android.util.Log;

import com.staceybellerose.randomwordgenerator.BuildConfig;
import com.staceybellerose.randomwordgenerator.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
     * Get the description of the word list
     *
     * @return the word list description
     */
    public String getWordListDescription() {
        final String wordListName = mSettings.getWordListName();
        final String[] listNames = mContext.getResources().getStringArray(R.array.word_list_resources);
        final String[] listDescriptions = mContext.getResources().getStringArray(R.array.word_list_descriptions);
        String result = null;
        for (int i = 0; i < listNames.length; i++) {
            final String listName = listNames[i];
            String description;
            try {
                description = listDescriptions[i];
                if (wordListName.equals(listName)) {
                    result = description;
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
                // since we don't have a proper entry, it can't match our list name
            }
        }
        return result;
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
        try {
            final InputStreamReader reader = new InputStreamReader(inputStream, "UTF8");
            final BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                processWord(wordList, line, minLength, maxLength);
            }
            bufferedReader.close();
            reader.close();
            inputStream.close();
        } catch (IOException exception) {
            Log.e(TAG, exception.getMessage(), exception);
        }
        return wordList;
    }

    /**
     * Process a word to determine if it should be added to the word list
     *
     * @param wordlist The word list to which the word should be added
     * @param checkWord the word to check
     * @param minLength the minimum length of allowed words
     * @param maxLength the maximum length of allowed words
     */
    private void processWord(final List<String> wordlist, final String checkWord, final int minLength,
                             final int maxLength) {
        final String word = checkWord.split("\t")[0];
        if (TextUtils.isEmpty(word)) {
            return;
        }
        if (word.length() < minLength || word.length() > maxLength) {
            return;
        }
        wordlist.add(word);
    }

    /**
     * Refresh the list of random words.
     *
     * @return a Flowable with the randomly selected words
     */
    public Flowable<String> refreshWords() {
        return Flowable.interval(TIME_BETWEEN_WORD_DISPLAY_MS, TimeUnit.MILLISECONDS, Schedulers.io())
                .map(number -> mSecureRandom.nextInt(mWordList.size()))
                .map(mWordList::get)
                .filter(substring -> !TextUtils.isEmpty(substring))
                .filter(substring -> mFilterList.indexOf(substring) == -1)
                .filter(substring -> (mCleanFilter == null) || mCleanFilter.indexOf(substring) == -1)
                .distinct()
                .take(mSettings.getDisplayCount());
    }
}
