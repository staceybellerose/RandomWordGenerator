package com.staceybellerose.randomwordgenerator.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.mikepenz.aboutlibraries.Libs;
import com.staceybellerose.randomwordgenerator.ChangelogActivity;
import com.staceybellerose.randomwordgenerator.SettingsActivity;
import com.staceybellerose.randomwordgenerator.WordListDetailsActivity;

/**
 * Listener to handle clicks on the Special buttons
 */
public class SpecialButtonListener extends SimpleLibsListener {
    @Override
    public boolean onExtraClicked(final View view, final Libs.SpecialButton specialButton) {
        final Context context = view.getContext();
        Intent intent = null;
        if (specialButton == Libs.SpecialButton.SPECIAL1) {
            intent = new Intent(context, SettingsActivity.class);
        } else if (specialButton == Libs.SpecialButton.SPECIAL2) {
            intent = new Intent(context, WordListDetailsActivity.class);
        } else if (specialButton == Libs.SpecialButton.SPECIAL3) {
            intent = new Intent(context, ChangelogActivity.class);
        }
        if (intent != null) {
            context.startActivity(intent);
            return true;
        }
        return super.onExtraClicked(view, specialButton);
    }
}
