package com.staceybellerose.randomwordgenerator.listeners;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.mikepenz.aboutlibraries.Libs;
import com.staceybellerose.randomwordgenerator.R;

/**
 * Listener to handle AboutLibraries fragment clicks
 */
public class SpecialButtonListener extends SimpleLibsListener {
    @Override
    public boolean onExtraClicked(final View view, final Libs.SpecialButton specialButton) {
        final Context context = view.getContext();
        if (specialButton == Libs.SpecialButton.SPECIAL1) {
            final Intent intent = new Intent(Intent.ACTION_VIEW, getAppUri(context));
            context.startActivity(intent);
            return true;
        }
        return super.onExtraClicked(view, specialButton);
    }

    @Override
    public void onIconClicked(final View view) {
        final Context context = view.getContext();
        final Intent intent = new Intent(Intent.ACTION_VIEW, getAppUri(context));
        context.startActivity(intent);
    }

    /**
     * Get the App Uri from Resources
     *
     * @param context the Context
     * @return a Uri containing the parsed string resource
     */
    private Uri getAppUri(final Context context) {
        final String gitBranch = context.getString(R.string.git_branch);
        return Uri.parse(context.getString(R.string.app_url, gitBranch));
    }
}
