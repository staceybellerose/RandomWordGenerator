package com.staceybellerose.randomwordgenerator;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.staceybellerose.randomwordgenerator.adapters.ListDetailsAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import jonathanfinerty.once.Once;

/**
 * Display a list of available word lists to the user and show some stats about each list.
 */
public class WordListDetailsActivity extends AppCompatActivity {

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

        if (!Once.beenDone(Once.THIS_APP_INSTALL, ONCE_HELP)) {
            showHelpFragment();
            Once.markDone(ONCE_HELP);
        }

        mRecyclerView.setHasFixedSize(true);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        final RecyclerView.Adapter adapter = new ListDetailsAdapter(this);
        mRecyclerView.setAdapter(adapter);
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

    /**
     * Show the Help bottom sheet
     */
    private void showHelpFragment() {
        final WordListHelpFragment helpFragment = WordListHelpFragment.newInstance();
        helpFragment.show(getSupportFragmentManager(), "help_fragment");
    }

}
