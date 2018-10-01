package com.staceybellerose.randomwordgenerator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TabStopSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.staceybellerose.randomwordgenerator.utils.Settings;
import com.staceybellerose.randomwordgenerator.utils.WordListManager;
import com.staceybellerose.randomwordgenerator.widgets.TourManager;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import jonathanfinerty.once.Once;

/**
 * The main activity for this app
 */
public class MainActivity extends AppCompatActivity {
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
    @SuppressWarnings("unused")
    private static final String TAG = "RandomWordGenerator";
    /**
     * Key to track when the help tour has been shown
     */
    private static final String ONCE_TOUR = "showAppTour";

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
     * Flag to indicate that we are using a downloaded list rather than a built-in list.
     */
    private boolean mUsingDownloadedList;
    /**
     * The width of mRandomContent, after rendering the layout, in pixels.
     */
    private int mContentWidth;
    /**
     * Help tour manager
     */
    private TourManager mTourManager;
    /**
     * Utility object to manage reading our shared preferences
     */
    private Settings mSettings;
    /**
     * Word list manager
     */
    private WordListManager mWordListManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mSettings = Settings.getInstance(this);
        mTourManager = new TourManager(this);
        mWordListManager = new WordListManager(this);
        mSwipeLayout.setColorSchemeColors(mColorAlternative, mColorPrimary, mColorAccent);
        mSwipeLayout.setOnRefreshListener(() -> new Handler().postDelayed(this::refreshWords, 250));

        if (savedInstanceState == null) {
            new Handler().postDelayed(() -> reloadLists(true), 250);
        } else {
            mRandomContent.setText(savedInstanceState.getCharSequence(STATE_WORDS));
            mWordListTitle.setText(savedInstanceState.getCharSequence(STATE_WORD_LIST_TITLE));
            new Handler().postDelayed(() -> reloadLists(false), 250);
        }

        // (re)set the tab stop for the random word content
        mRandomContent.post(() -> {
            mContentWidth = mRandomContent.getWidth();
            final SpannableString newContent = new SpannableString(mRandomContent.getText().toString());
            newContent.setSpan(new TabStopSpan.Standard(mContentWidth / 2), 0, newContent.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mRandomContent.setText(newContent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Once.beenDone(Once.THIS_APP_INSTALL, ONCE_TOUR)) {
            mTourManager.startTour();
            Once.markDone(ONCE_TOUR);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle savedInstanceState) {
        savedInstanceState.putCharSequence(STATE_WORDS, mRandomContent.getText());
        savedInstanceState.putCharSequence(STATE_WORD_LIST_TITLE, mWordListTitle.getText());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            return true;
        } else if (itemId == R.id.action_refresh) {
            refreshWords();
            return true;
        } else if (itemId == R.id.action_changelog) {
            final Intent intent = new Intent(this, ChangelogActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_wordlists) {
            final Intent intent = new Intent(this, WordListDetailsActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_help) {
            mTourManager.startTour();
            return true;
        } else if (itemId == R.id.action_about) {
            final Intent intent = new Intent(this, MyLibsActivity.class);
            startActivity(intent);
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
                    final String wordListName = mWordListManager.reloadWordList();
                    mUsingDownloadedList = (wordListName.equalsIgnoreCase(getString(R.string.pref_word_list_download)));
                    setWordListTitle(wordListName);
                }
                if (data.getBooleanExtra(getString(R.string.pref_clean_filter_changed), false)) {
                    mWordListManager.reloadCleanFilter();
                }
                refreshWords();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * copy list of random words (space separated) to clipboard
     */
    @OnClick(R.id.fab)
    @SuppressWarnings("unused")
    public void copyToClipboard() {
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        final String randomWords = mRandomContent.getText().toString();
        final ClipData clipData = ClipData.newPlainText("simple text", randomWords.replace("\n", " "));
        clipboard.setPrimaryClip(clipData);
        Snackbar.make(mFab, R.string.copied, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Reload the word lists and optionally the word display.
     *
     * @param refreshWords Flag indicating whether to refresh the display of words as well
     */
    private void reloadLists(final boolean refreshWords) {
        mSwipeLayout.setRefreshing(true);
        final String wordListName = mWordListManager.reloadWordList();
        setWordListTitle(wordListName);
        mWordListManager.reloadCleanFilter();
        if (refreshWords) {
            refreshWords();
        } else {
            mSwipeLayout.setRefreshing(false);
        }
    }

    /**
     * Set the word list title to the selected word list
     *
     * @param wordListName name of the selected word list, from @array/word_list_resources
     */
    private void setWordListTitle(final String wordListName) {
        final String[] listNames = getResources().getStringArray(R.array.word_list_resources);
        final String[] listTitles = getResources().getStringArray(R.array.word_list_descriptions);
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
     * Count the number of words in a word list.
     *
     * @param wordlist A string of words to count
     * @return the total number of words
     */
    private int countWords(final String wordlist) {
        final String trim = wordlist.trim();
        if (trim.isEmpty()) {
            return 0;
        } else {
            return trim.split("\\s+").length;
        }
    }

    /**
     * Add a new string to a list of words, making two tabbed columns of text.
     *
     * @param substring   The new string to add
     * @param maxCount    The maximum number of strings to be added
     * @param isTwoColumn Flag indicating whether two columns should be displayed
     */
    private void appendText(final String substring, final int maxCount, final boolean isTwoColumn) {
        final String currentText = mRandomContent.getText().toString();
        if (!isTwoColumn || countWords(currentText) < maxCount / 2) {
            mRandomContent.append(substring + "\n");
        } else {
            String[] lines = currentText.split("\n");
            final int lineToUpdate = countWords(currentText) - (maxCount / 2);
            lines[lineToUpdate] = lines[lineToUpdate] + "\t" + substring;
            final SpannableString newContent = new SpannableString(TextUtils.join("\n", lines));
            newContent.setSpan(new TabStopSpan.Standard(mContentWidth / 2), 0, newContent.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mRandomContent.setText(newContent);
        }
    }

    /**
     * Refresh the list of random words.
     */
    private void refreshWords() {
        if (mUsingDownloadedList) {
            mRandomContent.setText(getString(R.string.downloadable_content));
        } else {
            mSwipeLayout.setRefreshing(true);
            mRandomContent.setText("");
            mWordListManager.refreshWords()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(substring ->
                        appendText(substring, mSettings.getDisplayCount(), mSettings.isDisplayTwoColumns()),
                            Throwable::printStackTrace,
                            () -> mSwipeLayout.setRefreshing(false));
        }
    }
}
