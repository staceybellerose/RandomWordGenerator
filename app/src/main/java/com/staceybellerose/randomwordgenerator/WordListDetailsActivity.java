package com.staceybellerose.randomwordgenerator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.staceybellerose.randomwordgenerator.adapters.ListDetailsAdapter;
import com.staceybellerose.randomwordgenerator.utils.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jonathanfinerty.once.Once;

/**
 * Display a list of available word lists to the user and show some stats about each list.
 */
public class WordListDetailsActivity extends AppCompatActivity implements ListDetailsAdapter.OnItemClickListener,
        AdapterView.OnItemSelectedListener, ViewTreeObserver.OnGlobalLayoutListener {

    /**
     * Request code to indicate Word List Details Activity called
     */
    public static final int WORD_LIST_REQUEST_CODE = 314;
    /**
     * Key to track when the help fragment has automatically been shown
     */
    private static final String ONCE_HELP = "listHelpDisplay";
    /**
     * the toolbar
     */
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    /**
     * RecyclerView to display list of word lists
     */
    @BindView(R.id.details_list)
    RecyclerView mRecyclerView;
    /**
     * Data binding adapter for the recycler view
     */
    private ListDetailsAdapter mAdapter;
    /**
     * Layout manager for the recycler view
     */
    private LinearLayoutManager mLayoutManager;
    /**
     * List of available languages
     */
    private List<String> mLanguages;
    /**
     * Utility object to manage reading our shared preferences
     */
    private Settings mSettings;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list_details);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setResult(RESULT_CANCELED);
        mSettings = Settings.getInstance(this);
        initRecycler();

        if (!Once.beenDone(Once.THIS_APP_INSTALL, ONCE_HELP)) {
            showHelpFragment();
            Once.markDone(ONCE_HELP);
        }

        final ViewTreeObserver viewTreeObserver = mRecyclerView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(this);
    }

    /**
     * Initialize the Recycler View
     */
    private void initRecycler() {
        mAdapter = new ListDetailsAdapter(this);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mLanguages = new ArrayList<>(mAdapter.getLanguageSet());
        mLanguages.add(0, getString(R.string.action_list_filter).toUpperCase(Locale.getDefault()));
    }

    @Override
    public void onGlobalLayout() {
        mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        scrollToSelectedItem();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);

        final MenuItem item = menu.findItem(R.id.action_filter);
        final Spinner spinner = (Spinner) item.getActionView();

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mLanguages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        final String searchLanguage = mSettings.getSearchLanguage();
        if (!TextUtils.isEmpty(searchLanguage)) {
            final int position = adapter.getPosition(searchLanguage);
            spinner.setSelection(position);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_help) {
            showHelpFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(final int position, final View view) {
        mSettings.setWordListName(this, mAdapter.getItem(position).getResource());
        final Intent intent = new Intent();
        intent.putExtra(getString(R.string.pref_word_list_changed), true);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Show the Help bottom sheet
     */
    private void showHelpFragment() {
        final WordListHelpFragment helpFragment = WordListHelpFragment.newInstance();
        helpFragment.show(getSupportFragmentManager(), "help_fragment");
    }

    /**
     * Scroll the recycler view to the position of the selected word list
     */
    private void scrollToSelectedItem() {
        final int selectedPosition = mAdapter.getSelectedPosition();
        if (selectedPosition == -1) {
            return;
        }
        final View firstChild = mRecyclerView.getChildAt(0);
        // Assume that items all have the same height (or close to it)
        final int itemHeight = firstChild.getHeight();
        final int recyclerHeight = mRecyclerView.getHeight();
        final int offset = (recyclerHeight - itemHeight) / 4;
        mLayoutManager.scrollToPositionWithOffset(selectedPosition, offset);
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long ident) {
        final String searchString = (position == 0) ? "" : mLanguages.get(position);
        mAdapter.getFilter().filter(searchString);
        mSettings.setSearchLanguage(this, searchString);
    }

    @Override
    public void onNothingSelected(final AdapterView<?> parent) {
        mAdapter.getFilter().filter(null);
        mSettings.setSearchLanguage(this, "");
    }
}
