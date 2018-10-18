package com.staceybellerose.randomwordgenerator.listeners;

import android.view.View;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.aboutlibraries.entity.Library;

/**
 * A simple "do nothing" implementation of LibsListener, for when only a handful of methods need
 * to be implemented
 */
class SimpleLibsListener implements LibsConfiguration.LibsListener {
    @Override
    public void onIconClicked(final View view) {
    }

    @Override
    public boolean onLibraryAuthorClicked(final View view, final Library library) {
        return false;
    }

    @Override
    public boolean onLibraryContentClicked(final View view, final Library library) {
        return false;
    }

    @Override
    public boolean onLibraryBottomClicked(final View view, final Library library) {
        return false;
    }

    @Override
    public boolean onExtraClicked(final View view, final Libs.SpecialButton specialButton) {
        return false;
    }

    @Override
    public boolean onIconLongClicked(final View view) {
        return false;
    }

    @Override
    public boolean onLibraryAuthorLongClicked(final View view, final Library library) {
        return false;
    }

    @Override
    public boolean onLibraryContentLongClicked(final View view, final Library library) {
        return false;
    }

    @Override
    public boolean onLibraryBottomLongClicked(final View view, final Library library) {
        return false;
    }
}
