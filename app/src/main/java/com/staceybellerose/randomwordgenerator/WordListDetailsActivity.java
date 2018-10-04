package com.staceybellerose.randomwordgenerator;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.staceybellerose.randomwordgenerator.adapters.ListDetailsAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Display a list of available word lists to the user and show some stats about each list.
 */
public class WordListDetailsActivity extends AppCompatActivity {

    /**
     * Request code to indicate Word List Details Activity called
     */
    public static final int WORD_LIST_REQUEST_CODE = 314;
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

        mRecyclerView.setHasFixedSize(true);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        final RecyclerView.Adapter adapter = new ListDetailsAdapter(this);
        mRecyclerView.setAdapter(adapter);
    }

}
