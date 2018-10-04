package com.staceybellerose.randomwordgenerator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.staceybellerose.randomwordgenerator.adapters.ListDetailsAdapter;
import com.staceybellerose.randomwordgenerator.utils.Settings;

import butterknife.BindView;
import butterknife.ButterKnife;
import jonathanfinerty.once.Once;

/**
 * Display a list of available word lists to the user and show some stats about each list.
 */
public class WordListDetailsActivity extends AppCompatActivity implements ListDetailsAdapter.OnItemClickListener {

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

        if (!Once.beenDone(Once.THIS_APP_INSTALL, ONCE_HELP)) {
            showHelpFragment();
            Once.markDone(ONCE_HELP);
        }

        mAdapter = new ListDetailsAdapter(this);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
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

}
