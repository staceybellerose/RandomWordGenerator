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
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class MainActivity extends AppCompatActivity {
    private static final int TOUR_REFRESH_ICON_SIZE_DP = 48;

    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.random_content) TextView mRandomContent;
    @BindView(R.id.swiperefresh) SwipeRefreshLayout mSwipeLayout;
    @BindColor(R.color.colorPrimary) int mColorPrimary;
    @BindColor(R.color.colorAccent) int mColorAccent;
    @BindColor(R.color.colorAlternative) int mColorAlternative;
    @BindString(R.string.title_activity_changelog) String mChangelogActivityTitle;
    @BindString(R.string.title_activity_settings) String mSettingsActivityTitle;
    @BindString(R.string.app_name) String mAppName;
    @BindString(R.string.app_long_desc) String mAppLongDesc;

    SecureRandom secureRandom = new SecureRandom();
    ArrayList<String> mWordList = new ArrayList<>();
    int mRandomRange;
    int mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    @OnClick(R.id.fab)
    public void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String randomWords = mRandomContent.getText().toString();
        ClipData clipData = ClipData.newPlainText("simple text", randomWords.replace("\n", " "));
        clipboard.setPrimaryClip(clipData);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // TODO start Settings activity here
            return true;
        } else if (id == R.id.action_refresh) {
            refreshWords();
            return true;
        } else if (id == R.id.action_changelog) {
            Intent intent = new Intent(this, ChangelogActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_help) {
            startTour();
            return true;
        } else if (id == R.id.action_about) {
            showAbout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshWords() {
        mRandomContent.setText("");
        Observable.interval(100, TimeUnit.MILLISECONDS, Schedulers.io())
                .map(i -> secureRandom.nextInt(mRandomRange))
                .map(i -> mWordList.get(i))
                .filter(s -> s != null)
                .map(s -> s + "\n")
                .distinct()
                .take(mCount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> mRandomContent.append(s));
    }

    private void readWordList() {
        InputStream in = getResources().openRawResource(R.raw.wordlist);
        InputStreamReader reader = new InputStreamReader(in);

        StringObservable.byLine(StringObservable.from(reader))
                .filter(s -> !TextUtils.isEmpty(s))
                .subscribe(s -> mWordList.add(s));
        mRandomRange = mWordList.size();

        try {
            reader.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                    }

                    @Override
                    public void onHidePromptComplete() {
                        showTourPullRefresh();
                    }
                })
                .show();
    }

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
