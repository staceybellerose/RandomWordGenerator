package com.staceybellerose.randomwordgenerator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RawRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TabStopSpan;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import com.github.davidmoten.rx2.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jonathanfinerty.once.Once;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * The main activity for this app
 */
public class MainActivity extends AppCompatActivity {
    /**
     * size of the swipe to refresh help icon
     */
    private static final int TOUR_REFRESH_ICON_SIZE_DP = 48;
    /**
     * Time in milliseconds to delay between new words displaying
     */
    private static final int TIME_BETWEEN_WORD_DISPLAY_MS = 100;
    /**
     * Request code to indicate Settings Activity called
     */
    private static final int SETTINGS_REQUEST_CODE = 42;
    /**
     * key used for saving selected words to instance state
     */
    private static final String STATE_WORDS = "stateWords";
    /**
     * key used to save word list title to instance state
     */
    private static final String STATE_WORD_LIST_TITLE = "stateWordListTitle";
    /**
     * tag string for error logging
     */
    private static final String TAG = "RandomWordGenerator";

    /**
     * the floating action button
     */
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    /**
     * the toolbar
     */
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    /**
     * textview containing the randomly selected word list
     */
    @BindView(R.id.random_content)
    TextView mRandomContent;
    /**
     * textview containing the name of the word list
     */
    @BindView(R.id.word_list_title)
    TextView mWordListTitle;
    /**
     * swipe down to refresh layout
     */
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeLayout;
    /**
     * the primary color for the app
     */
    @BindColor(R.color.colorPrimary)
    int mColorPrimary;
    /**
     * the accent color for the app
     */
    @BindColor(R.color.colorAccent)
    int mColorAccent;
    /**
     * a third color for the app, used in the swipe to refresh progress animation
     */
    @BindColor(R.color.colorAlternative)
    int mColorAlternative;
    /**
     * cryptographically secure random number generator
     */
    private SecureRandom mSecureRandom = new SecureRandom();
    /**
     * The word list to use in generating randomly selected words.
     */
    private ArrayList<String> mWordList = new ArrayList<>();
    /**
     * The list of slur words to filter from the final selection.
     */
    private ArrayList<String> mFilterList = new ArrayList<>();
    /**
     * The list of vulgar words to filter from the final selection.
     */
    private ArrayList<String> mCleanFilter;
    /**
     * Flag to indicate that we are using a downloaded list rather than a built-in list.
     */
    private boolean mUsingDownloadedList;
    /**
     * The width of mRandomContent, after rendering the layout, in pixels.
     */
    private int mContentWidth;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mSwipeLayout.setColorSchemeColors(mColorAlternative, mColorPrimary, mColorAccent);
        mSwipeLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            refreshWords();
            mSwipeLayout.setRefreshing(false);
        }, TIME_BETWEEN_WORD_DISPLAY_MS));
        if (savedInstanceState != null) {
            mRandomContent.setText(savedInstanceState.getCharSequence(STATE_WORDS));
            mWordListTitle.setText(savedInstanceState.getCharSequence(STATE_WORD_LIST_TITLE));
            new Handler().postDelayed(() -> {
                reloadWordList();
                reloadCleanFilter();
            }, 250);
        } else {
            mSwipeLayout.setRefreshing(true);
            new Handler().postDelayed(() -> {
                reloadWordList();
                reloadCleanFilter();
                mSwipeLayout.setRefreshing(false);
                refreshWords();
            }, 250);
        }
        // (re)set the tab stop for the random word content
        mRandomContent.post(() -> {
            mContentWidth = mRandomContent.getWidth();
            SpannableString newContent = new SpannableString(mRandomContent.getText().toString());
            newContent.setSpan(new TabStopSpan.Standard(mContentWidth / 2), 0, newContent.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mRandomContent.setText(newContent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String showAppTour = "showAppTour";
        if (!Once.beenDone(Once.THIS_APP_INSTALL, showAppTour)) {
            startTour();
            Once.markDone(showAppTour);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putCharSequence(STATE_WORDS, mRandomContent.getText());
        savedInstanceState.putCharSequence(STATE_WORD_LIST_TITLE, mWordListTitle.getText());
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * copy list of random words (space separated) to clipboard
     */
    @OnClick(R.id.fab)
    @SuppressWarnings("unused")
    public void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String randomWords = mRandomContent.getText().toString();
        ClipData clipData = ClipData.newPlainText("simple text", randomWords.replace("\n", " "));
        clipboard.setPrimaryClip(clipData);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            return true;
        } else if (itemId == R.id.action_refresh) {
            refreshWords();
            return true;
        } else if (itemId == R.id.action_changelog) {
            Intent intent = new Intent(this, ChangelogActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_wordlists) {
            Intent intent = new Intent(this, WordListDetailsActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_help) {
            startTour();
            return true;
        } else if (itemId == R.id.action_about) {
            showAbout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mSwipeLayout.setRefreshing(true);
                if (data.getBooleanExtra(getString(R.string.pref_word_list_changed), false)) {
                    reloadWordList();
                }
                if (data.getBooleanExtra(getString(R.string.pref_clean_filter_changed), false)) {
                    reloadCleanFilter();
                }
                mSwipeLayout.setRefreshing(false);
                refreshWords();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Get the maximum and minimum word lengths to display from shared preferences.
     *
     * @return an int array containing the min and max lengths as {min, max}
     */
    private int[] getWordLengths() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean unlimitedWordSize = sharedPref.getBoolean(getString(R.string.pref_unlimited_length_flag), true);
        int minLength;
        int maxLength;
        if (unlimitedWordSize) {
            minLength = getResources().getInteger(R.integer.default_min_word_length);
            maxLength = getResources().getInteger(R.integer.default_max_word_length);
        } else {
            minLength = sharedPref.getInt(getString(R.string.pref_min_word_length),
                    getResources().getInteger(R.integer.default_min_word_length));
            maxLength = sharedPref.getInt(getString(R.string.pref_max_word_length),
                    getResources().getInteger(R.integer.default_max_word_length));
        }
        if (minLength > maxLength) {
            // swap values and store the corrected values in shared preferences
            int temp = minLength;
            minLength = maxLength;
            maxLength = temp;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.pref_min_word_length), minLength);
            editor.putInt(getString(R.string.pref_max_word_length), maxLength);
            editor.apply();
        }
        return new int[] {minLength, maxLength};
    }

    /**
     * Reload the word list based on the one set in shared preferences.
     */
    private void reloadWordList() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int wordListId;
        String wordListName = sharedPref.getString(getString(R.string.pref_word_list),
                getString(R.string.pref_word_list_default));
        mUsingDownloadedList = (wordListName.equalsIgnoreCase(getString(R.string.pref_word_list_download)));
        if (!mUsingDownloadedList) {
            wordListId = getResources().getIdentifier(wordListName, "raw", this.getPackageName());
            if (wordListId == 0) {
                // something bad happened and we don't have a proper resource name; default to original word list
                wordListId = R.raw.wordlist;
            }
            int[] wordLengths = getWordLengths();
            mWordList = readWordList(wordListId, wordLengths[0], wordLengths[1]);
            mFilterList = readWordList(R.raw.filter_slurs, 0, Integer.MAX_VALUE);
            setWordListTitle(wordListName);
        }
    }

    /**
     * Set the word list title to the selected word list
     *
     * @param wordListName name of the selected word list, from @array/word_list_resources
     */
    private void setWordListTitle(final String wordListName) {
        String[] listNames = getResources().getStringArray(R.array.word_list_resources);
        String[] listTitles = getResources().getStringArray(R.array.word_list_descriptions);
        String title = null;
        for (int i = 0; i < listNames.length; i++) {
            if (listNames[i].equalsIgnoreCase(wordListName)) {
                title = listTitles[i];
            }
        }
        if (title == null) {
            title = listTitles[0];
        }
        mWordListTitle.setText(title);
    }

    /**
     * Reload the clean words filter based on shared preferences setting.
     */
    private void reloadCleanFilter() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean cleanWordsOnly = sharedPref.getBoolean(getString(R.string.pref_clean_words_flag),
                BuildConfig.CLEAN_WORDS_ONLY);

        if (BuildConfig.CLEAN_WORDS_ONLY || cleanWordsOnly) {
            mCleanFilter = readWordList(R.raw.filter_clean, 0, Integer.MAX_VALUE);
        } else {
            mCleanFilter = null;
        }
    }

    /**
     * Count the number of words in a word list.
     *
     * @param wordlist A string of words to count
     * @return the total number of words
     */
    private int countWords(final String wordlist) {
        String trim = wordlist.trim();
        if (trim.isEmpty()) {
            return 0;
        } else {
            return trim.split("\\s+").length;
        }
    }

    /**
     * Add a new string to a list of words, making two tabbed columns of text.
     *
     * @param substring The new string to add
     * @param maxCount The maximum number of strings to be added
     * @param isTwoColumn Flag indicating whether two columns should be displayed
     */
    private void appendText(final String substring, final int maxCount, final boolean isTwoColumn) {
        String currentText = mRandomContent.getText().toString();
        if (!isTwoColumn || countWords(currentText) < maxCount / 2) {
            mRandomContent.append(substring + "\n");
        } else {
            String[] lines = currentText.split("\n");
            int lineToUpdate = countWords(currentText) - (maxCount / 2);
            lines[lineToUpdate] = lines[lineToUpdate] + "\t" + substring;
            SpannableString newContent = new SpannableString(TextUtils.join("\n", lines));
            newContent.setSpan(new TabStopSpan.Standard(mContentWidth / 2), 0, newContent.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mRandomContent.setText(newContent);
        }
    }

    /**
     * Refresh the list of random words.
     */
    private void refreshWords() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int count = sharedPref.getInt(getString(R.string.pref_display_count),
                getResources().getInteger(R.integer.default_display_count));
        boolean twoColumn = sharedPref.getBoolean(getString(R.string.pref_two_column),
                getResources().getBoolean(R.bool.pref_two_column_default));

        if (mUsingDownloadedList) {
            mRandomContent.setText(getString(R.string.downloadable_content));
        } else {
            mRandomContent.setText("");
            Observable.interval(TIME_BETWEEN_WORD_DISPLAY_MS, TimeUnit.MILLISECONDS, Schedulers.io())
                    .map(number -> mSecureRandom.nextInt(mWordList.size()))
                    .map(mWordList::get)
                    .filter(substring -> substring != null)
                    .filter(substring -> mFilterList.indexOf(substring) == -1)
                    .filter(substring -> (mCleanFilter == null) || mCleanFilter.indexOf(substring) == -1)
                    .distinct()
                    .take(count)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(substring -> appendText(substring, count, twoColumn));
        }
    }

    /**
     * Read the list of words from a raw resource file.
     *
     * @param wordListResource A raw resource containing the word list to process
     * @param minLength The minimum word length
     * @param maxLength The maximum word length
     * @return The list of words present in the word list
     */
    private ArrayList<String> readWordList(@RawRes final int wordListResource, final int minLength,
                                           final int maxLength) {
        ArrayList<String> wordList = new ArrayList<>();
        InputStream inputStream = getResources().openRawResource(wordListResource);
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
     * Start the help tour by highlighting the "copy to clipboard" button.
     */
    private void startTour() {
        new MaterialTapTargetPrompt.Builder(this, R.style.AppTheme_Light)
                .setPrimaryText(R.string.tour_clipboard_primary)
                .setSecondaryText(R.string.tour_clipboard_secondary)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                .setTarget(mFab)
                .setPromptStateChangeListener((prompt, state) -> {
                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
                            || state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                        showTourPullRefresh();
                    }
                })
                .show();
    }

    /**
     * Continue the help tour by highlighting the "swipe down to refresh" functionality.
     */
    private void showTourPullRefresh() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int targetLeft = size.x / 2;
        Rect rect = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        int iconHeight = (int) (Resources.getSystem().getDisplayMetrics().density
                * TOUR_REFRESH_ICON_SIZE_DP);
        int targetTop = mToolbar.getHeight() + statusBarHeight + iconHeight;
        new MaterialTapTargetPrompt.Builder(this, R.style.AppTheme_Light)
                .setPrimaryText(R.string.tour_refresh_primary)
                .setSecondaryText(R.string.tour_refresh_secondary)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                .setTarget(targetLeft, targetTop)
                .setIcon(R.drawable.ic_arrow_downward_black_48dp)
                .show();
    }

    /**
     * Show the About activity.
     */
    private void showAbout() {
        Intent intent = new Intent(this, MyLibsActivity.class);
        startActivity(intent);
    }
}
