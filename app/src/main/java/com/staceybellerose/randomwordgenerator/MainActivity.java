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
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

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
import jonathanfinerty.once.Once;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.observables.StringObservable;
import rx.schedulers.Schedulers;
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
     * key used for saving instance state
     */
    private static final String STATE_WORD_LIST_KEY = "stateWordList";
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
            mRandomContent.setText(savedInstanceState.getString(STATE_WORD_LIST_KEY));
        } else {
            refreshWords();
        }
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
        savedInstanceState.putString(STATE_WORD_LIST_KEY, mRandomContent.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * copy list of random words (space separated) to clipboard
     */
    @OnClick(R.id.fab)
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
                refreshWords();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * refresh the list of random words
     */
    private void refreshWords() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int count = sharedPref.getInt(getString(R.string.pref_display_count),
                getResources().getInteger(R.integer.default_display_count));
        boolean cleanWordsOnly = sharedPref.getBoolean(getString(R.string.pref_clean_words_flag),
                BuildConfig.CLEAN_WORDS_ONLY);
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
        int wordListId;
        if (BuildConfig.CLEAN_WORDS_ONLY || cleanWordsOnly) {
            wordListId = R.raw.cleanwordlist;
        } else {
            wordListId = R.raw.wordlist;
        }
        ArrayList<String> wordList = readWordList(wordListId, minLength, maxLength);
        mRandomContent.setText("");
        Observable.interval(TIME_BETWEEN_WORD_DISPLAY_MS, TimeUnit.MILLISECONDS, Schedulers.io())
                .map(number -> mSecureRandom.nextInt(wordList.size()))
                .map(wordList::get)
                .filter(substring -> substring != null)
                .map(substring -> substring + "\n")
                .distinct()
                .take(count)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(substring -> mRandomContent.append(substring));
    }

    /**
     * read the list of words from a raw resource file
     *
     * @param wordlistResource A raw resource containing the word list to process
     * @param minLength The minimum word length
     * @param maxLength The maximum word length
     * @return The list of words present in the word list
     */
    private ArrayList<String> readWordList(@RawRes final int wordlistResource, final int minLength,
                                           final int maxLength) {
        ArrayList<String> wordList = new ArrayList<>();
        InputStream inputStream = getResources().openRawResource(wordlistResource);
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(inputStream, "UTF8");
        } catch (UnsupportedEncodingException exception) {
            Log.e(TAG, exception.getMessage(), exception);
        }

        StringObservable.byLine(StringObservable.from(reader))
                .filter(substring -> !TextUtils.isEmpty(substring))
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
     * start the help tour by highlighting the "copy to clipboard" button
     */
    private void startTour() {
        new MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText(R.string.tour_clipboard_primary)
                .setSecondaryText(R.string.tour_clipboard_secondary)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                .setTarget(mFab)
                .setBackgroundColourFromRes(R.color.colorPrimaryLight)
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                    @Override
                    public void onHidePrompt(final MotionEvent event, final boolean tappedTarget) {
                    }

                    @Override
                    public void onHidePromptComplete() {
                        showTourPullRefresh();
                    }
                })
                .show();
    }

    /**
     * continue the help tour by highlighting the "swipe down to refresh" functionality
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
        new MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText(R.string.tour_refresh_primary)
                .setSecondaryText(R.string.tour_refresh_secondary)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                .setTarget(targetLeft, targetTop)
                .setIcon(R.drawable.ic_pulldown)
                .setBackgroundColourFromRes(R.color.colorPrimaryLight)
                .show();
    }

    /**
     * show the About activity
     */
    private void showAbout() {
        Intent intent = new Intent(this, MyLibsActivity.class);
        startActivity(intent);
    }
}
