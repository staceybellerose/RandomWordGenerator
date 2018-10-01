package com.staceybellerose.randomwordgenerator.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staceybellerose.randomwordgenerator.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Provide a binding from an app-specific data set to views that are displayed within a RecyclerView
 */
public class ListDetailsAdapter extends RecyclerView.Adapter<ListDetailsAdapter.DetailsViewHolder> {
    /**
     * the list to display
     */
    private final List<WordList> mWordList = new ArrayList<>();

    /**
     * Constructor
     *
     * @param context the activity context
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public ListDetailsAdapter(final Context context) {
        final Resources res = context.getResources();
        final String[] listNames = res.getStringArray(R.array.word_list_descriptions);
        final String[] sources = res.getStringArray(R.array.list_sources);
        final int[] counts = res.getIntArray(R.array.list_lengths);
        for (int i = 0; i < listNames.length; i++) {
            final String name = listNames[i];
            String source;
            int count;
            try {
                source = sources[i];
            } catch (ArrayIndexOutOfBoundsException e) {
                source = "unknown";
            }
            try {
                count = counts[i];
            } catch (ArrayIndexOutOfBoundsException e) {
                count = 0;
            }
            mWordList.add(new WordList(name, source, count));
        }
    }

    @Override
    public DetailsViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.word_list_details_item, parent, false);
        return new DetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DetailsViewHolder holder, final int position) {
        final Context context = holder.getContext();
        final WordList wordList = mWordList.get(position);
        holder.getListName().setText(wordList.getListName());
        holder.getSourceText().setText(wordList.getSource());
        holder.getCountText().setText(String.valueOf(wordList.getWordCount()));
        holder.getEntropyText().setText(context.getString(R.string.label_entropy, wordList.getEntropy()));
    }

    @Override
    public int getItemCount() {
        return mWordList.size();
    }

    /**
     *  Describe an item view and metadata about its place within the RecyclerView
     */
    static class DetailsViewHolder extends RecyclerView.ViewHolder {

        /**
         * Text view containing the word list name
         */
        @BindView(R.id.word_list_name)
        TextView mListName;
        /**
         * Text view containing the word list source
         */
        @BindView(R.id.source_text)
        TextView mSourceText;
        /**
         * Text view containing the word count of the list
         */
        @BindView(R.id.count_text)
        TextView mCountText;
        /**
         * Text view containing the entropy when selecting a single word from the list
         */
        @BindView(R.id.entropy_text)
        TextView mEntropyText;

        /**
         * Constructor
         *
         * @param view the item view
         */
        DetailsViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        /**
         * Get the TextView displaying the list name
         * @return the list name TextView
         */
        TextView getListName() {
            return mListName;
        }

        /**
         * Get the TextView displaying the list source
         *
         * @return the list source TextView
         */
        TextView getSourceText() {
            return mSourceText;
        }

        /**
         * Get the TextView displaying the word count of the list
         *
         * @return the word count TextView
         */
        TextView getCountText() {
            return mCountText;
        }

        /**
         * Get the TextView displaying the list entropy
         *
         * @return the entropy TextView
         */
        TextView getEntropyText() {
            return mEntropyText;
        }

        /**
         * Get the Context of the view
         *
         * @return the context
         */
        Context getContext() {
            return mListName.getContext();
        }
    }

    /**
     * Data about a word list
     */
    private static final class WordList {
        /**
         * The name of a word list
         */
        private final String mListName;
        /**
         * The source of a word list
         */
        private final String mSource;
        /**
         * The number of words in the list
         */
        private final int mWordCount;

        /**
         * Constructor
         *
         * @param listName the list name
         * @param source the list source
         * @param wordCount the word count
         */
        WordList(final String listName, final String source, final int wordCount) {
            mListName = listName;
            mSource = source;
            mWordCount = wordCount;
        }

        /**
         * Get the list name
         * @return the list name
         */
        String getListName() {
            return mListName;
        }

        /**
         * Get the list source
         * @return the list source
         */
        String getSource() {
            return mSource;
        }

        /**
         * Get the word count of the list
         * @return the word count
         */
        int getWordCount() {
            return mWordCount;
        }

        /**
         * Get the amount of entropy when randomly selecting a word from the list
         * @return the amount of entropy
         */
        double getEntropy() {
            final double entropy = Math.log(mWordCount) / Math.log(2);
            return Math.round(entropy * 10.0) / 10.0;
        }
    }
}
