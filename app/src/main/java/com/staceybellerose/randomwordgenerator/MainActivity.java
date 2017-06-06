package com.staceybellerose.randomwordgenerator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
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
    /**
     * the word list, from which random words are selected
     */
    private ArrayList<String> mWordList = new ArrayList<>();
    /**
     * number of random words to display
     */
    private int mCount;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        readWordList();
        mSwipeLayout.setColorSchemeColors(mColorAlternative, mColorPrimary, mColorAccent);
        mSwipeLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            refreshWords();
            mSwipeLayout.setRefreshing(false);
        }, 1000));
        // TODO Read this count from preferences once Settings activity is built
        mCount = 12;
        refreshWords();
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
            // TODO start Settings activity here
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

    /**
     * refresh the list of random words
     */
    private void refreshWords() {
        mRandomContent.setText("");
        Observable.interval(100, TimeUnit.MILLISECONDS, Schedulers.io())
                .map(number -> mSecureRandom.nextInt(mWordList.size()))
                .map(number -> mWordList.get(number))
                .filter(substring -> substring != null)
                .map(substring -> substring + "\n")
                .distinct()
                .take(mCount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(substring -> mRandomContent.append(substring));
    }

    /**
     * read the list of words from a raw resource file
     */
    private void readWordList() {
        InputStream inputStream = getResources().openRawResource(R.raw.wordlist);
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(inputStream, "UTF8");
        } catch (UnsupportedEncodingException exception) {
            Log.e(TAG, exception.getMessage(), exception);
        }

        StringObservable.byLine(StringObservable.from(reader))
                .filter(substring -> !TextUtils.isEmpty(substring))
                .subscribe(substring -> mWordList.add(substring));

        try {
            reader.close();
            inputStream.close();
        } catch (IOException exception) {
            Log.e(TAG, exception.getMessage(), exception);
        }
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
