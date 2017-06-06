package com.staceybellerose.randomwordgenerator.listeners;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.mikepenz.aboutlibraries.Libs;
import com.staceybellerose.randomwordgenerator.ChangelogActivity;

/**
 * Listener to handle clicks on the ChangeLog button
 */
public class ChangeLogButtonListener extends SimpleLibsListener {
    @Override
    public boolean onExtraClicked(final View view, final Libs.SpecialButton specialButton) {
        Activity context = (Activity) view.getContext();
        if (specialButton == Libs.SpecialButton.SPECIAL1) {
            // TODO start Settings activity here
            return true;
        } else if (specialButton == Libs.SpecialButton.SPECIAL2) {
            Intent intent = new Intent(context, ChangelogActivity.class);
            context.startActivity(intent);
            return true;
        }
        return super.onExtraClicked(view, specialButton);
    }
}
