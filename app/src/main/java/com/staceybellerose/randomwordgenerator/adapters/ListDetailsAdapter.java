package com.staceybellerose.randomwordgenerator.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staceybellerose.randomwordgenerator.R;
import com.staceybellerose.randomwordgenerator.utils.Settings;

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
     * item click listener
     */
    private OnItemClickListener mItemClickListener;
    /**
     * The currently selected word list
     */
    private final String mSelectedItem;

    /**
     * Constructor
     *
     * @param context the activity context
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public ListDetailsAdapter(final Context context) {
        final Resources res = context.getResources();
        final Settings settings = Settings.getInstance(context);
        mSelectedItem = settings.getWordListName();
        final String[] listNames = res.getStringArray(R.array.word_list_descriptions);
        final String[] listResources = res.getStringArray(R.array.word_list_resources);
        final String[] sources = res.getStringArray(R.array.list_sources);
        final String[] languages = res.getStringArray(R.array.list_language);
        final int[] counts = res.getIntArray(R.array.list_lengths);
        for (int i = 0; i < listNames.length; i++) {
            final String language = getStringFromArray(languages, i, "??");
            final String resource = getStringFromArray(listResources, i, "");
            final WordList wordList = new WordList();
            wordList.setListName(listNames[i]);
            wordList.setSource(getStringFromArray(sources, i, "unknown"));
            wordList.setWordCount(getIntFromArray(counts, i, 0));
            wordList.setLanguage(language);
            wordList.setResource(resource);
            wordList.setIsSelected(mSelectedItem.equals(resource));
            mWordList.add(wordList);
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
        final TextView listNameView = holder.getListName();
        listNameView.setText(wordList.getListName());
        holder.getLanguageText().setText(wordList.getLanguageText());
        holder.getSourceText().setText(wordList.getAuthor(context));
        holder.getCountText().setText(String.valueOf(wordList.getWordCount()));
        holder.getEntropyText().setText(context.getString(R.string.label_entropy, wordList.getEntropy()));
        holder.setItemClickListener(mItemClickListener);
        if (wordList.getResource().equals(mSelectedItem)) {
            listNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_favorite_green_500_36dp, 0);
        }
    }

    @Override
    public int getItemCount() {
        return mWordList.size();
    }

    /**
     * Register a callback to be invoked when an item in this Adapter has been clicked.
     *
     * @param itemClickListener The callback that will be invoked. This value may be null.
     */
    public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    /**
     * Gets the data associated with the specified position in the list
     *
     * @param position Which data to get
     * @return The data associated with the specified position in the list
     */
    public WordList getItem(final int position) {
        return mWordListFiltered.get(position);
    }

    /**
     * Get a string from an array of strings
     *
     * @param strings the array to process
     * @param position the position to retrieve
     * @param defaultValue the value to use if unable to retrieve the proper string
     * @return the strung at the provided position in the array
     */
    private String getStringFromArray(final String[] strings, final int position, final String defaultValue) {
        String result = defaultValue;
        try {
            result = strings[position];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        return result;
    }

    /**
     * Get an integer from an array of ints
     *
     * @param ints the array to process
     * @param position the position to retrieve
     * @param defaultValue the value to use if unable to retrieve the proper integer
     * @return the integer at the provided position in the array
     */
    private int getIntFromArray(final int[] ints, final int position, final int defaultValue) {
        int result = defaultValue;
        try {
            result = ints[position];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        return result;
    }
    /**
     *  Describe an item view and metadata about its place within the RecyclerView
     */
    static class DetailsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        /**
         * Text view containing the word list name
         */
        @BindView(R.id.word_list_name)
        TextView mListName;
        /**
         * Text view containing the language of the word list
         */
        @BindView(R.id.language_text)
        TextView mLanguageText;
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
         * A listener for item clicks
         */
        private OnItemClickListener mItemClickListener;

        /**
         * Constructor
         *
         * @param view the item view
         */
        DetailsViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(final View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(getAdapterPosition(), view);
            }
        }

        /**
         * Register a callback to be invoked when an item in this Adapter has been clicked.
         *
         * @param itemClickListener The callback that will be invoked. This value may be null.
         */
        void setItemClickListener(final OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        /**
         * Get the TextView displaying the list name
         * @return the list name TextView
         */
        TextView getListName() {
            return mListName;
        }

        /**
         * Get the TextView displaying the language
         * @return the language TextView
         */
        TextView getLanguageText() {
            return mLanguageText;
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
     * Interface definition for a callback to be invoked when an item in this Adapter has been clicked.
     */
    public interface OnItemClickListener {
        /**
         * Callback method to be invoked when an item in this Adapter has been clicked.
         *
         * @param position The position of the view in the adapter.
         * @param view The view within the AdapterView that was clicked (this will be a view provided by the adapter)
         */
        void onItemClick(int position, View view);
    }
}
