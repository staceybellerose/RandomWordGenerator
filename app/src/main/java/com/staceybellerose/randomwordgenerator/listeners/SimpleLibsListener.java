package com.staceybellerose.randomwordgenerator.listeners;

import android.view.View;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.aboutlibraries.entity.Library;

public class SimpleLibsListener implements LibsConfiguration.LibsListener {
    @Override
    public void onIconClicked(View v) {
    }

    @Override
    public boolean onLibraryAuthorClicked(View v, Library library) {
        return false;
    }

    @Override
    public boolean onLibraryContentClicked(View v, Library library) {
        return false;
    }

    @Override
    public boolean onLibraryBottomClicked(View v, Library library) {
        return false;
    }

    @Override
    public boolean onExtraClicked(View v, Libs.SpecialButton specialButton) {
        return false;
    }

    @Override
    public boolean onIconLongClicked(View v) {
        return false;
    }

    @Override
    public boolean onLibraryAuthorLongClicked(View v, Library library) {
        return false;
    }

    @Override
    public boolean onLibraryContentLongClicked(View v, Library library) {
        return false;
    }

    @Override
    public boolean onLibraryBottomLongClicked(View v, Library library) {
        return false;
    }
}
