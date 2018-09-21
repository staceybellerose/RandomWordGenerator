package com.staceybellerose.randomwordgenerator.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.mikepenz.aboutlibraries.Libs;
import com.staceybellerose.randomwordgenerator.ChangelogActivity;
import com.staceybellerose.randomwordgenerator.SettingsActivity;

/**
 * Listener to handle clicks on the Special buttons
 */
public class SpecialButtonListener extends SimpleLibsListener {
    @Override
    public boolean onExtraClicked(final View view, final Libs.SpecialButton specialButton) {
        Context context = view.getContext();
        if (specialButton == Libs.SpecialButton.SPECIAL1) {
            Intent intent = new Intent(context, SettingsActivity.class);
            context.startActivity(intent);
            return true;
        } else if (specialButton == Libs.SpecialButton.SPECIAL2) {
            Intent intent = new Intent(context, ChangelogActivity.class);
            context.startActivity(intent);
            return true;
        }
        return super.onExtraClicked(view, specialButton);
    }
}
