package com.staceybellerose.randomwordgenerator.widgets;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.Display;
import android.view.Window;

import com.staceybellerose.randomwordgenerator.R;
import com.staceybellerose.randomwordgenerator.utils.DimmedPromptBackground;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * Help tour manager
 */
public class TourManager {
    /**
     * the calling activity
     */
    private final Activity mActivity;

    /**
     * Constructor
     *
     * @param activity the calling activity
     */
    public TourManager(final Activity activity) {
        mActivity = activity;
    }

    /**
     * Start the help tour by highlighting the "copy to clipboard" button.
     */
    public void startTour() {
        new MaterialTapTargetPrompt.Builder(mActivity, R.style.MaterialTapTargetPromptTheme_Fab)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setIcon(R.drawable.ic_content_copy_white_24dp)
                .setIconDrawableTintMode(null)
                .setPromptBackground(new DimmedPromptBackground())
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
        final Point target = calculateRefreshTourTarget();
        new MaterialTapTargetPrompt.Builder(mActivity, R.style.MaterialTapTargetPromptTheme_Refresh)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setTarget(target.x, target.y)
                .setIcon(R.drawable.ic_arrow_down_white_24dp)
                .setIconDrawableTintMode(null)
                .setPromptBackground(new DimmedPromptBackground())
                .show();
    }

    /**
     * Calculate the target point for "pull to refresh" in the help tour
     *
     * @return a Point containing the target location
     */
    @SuppressWarnings("deprecation")
    private Point calculateRefreshTourTarget() {
        final Display display = mActivity.getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        final int iconSize = mActivity.getResources()
                .getDrawable(R.drawable.ic_arrow_down_white_24dp).getIntrinsicWidth();
        final int targetLeft = size.x / 2;
        final Rect rect = new Rect();
        final Window window = mActivity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        final int statusBarHeight = rect.top;
        final ActionBar actionBar = mActivity.getActionBar();
        int toolbarHeight;
        if (actionBar == null) {
            toolbarHeight = 0;
        } else {
            toolbarHeight = actionBar.getHeight();
        }
        final int targetTop = toolbarHeight + statusBarHeight + (iconSize / 2);
        return new Point(targetLeft, targetTop);
    }

}
