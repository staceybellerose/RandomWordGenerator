package com.staceybellerose.randomwordgenerator.adapters;

import android.content.Context;

import java.util.Locale;

/**
 * Data about a word list
 */
public final class WordList {
    /**
     * The name of a word list
     */
    private String mListName;
    /**
     * The identifier for the word list
     */
    private String mResource;
    /**
     * The source of a word list
     */
    private String mSource;
    /**
     * The number of words in the list
     */
    private int mWordCount;
    /**
     * The language used in the list
     */
    private String mLanguage;
    /**
     * Flag indicating whether this list is selected
     */
    private boolean mIsSelected;

    /**
     * Constructor
     */
    WordList() {
    }

    void setListName(final String listName) {
        this.mListName = listName;
    }

    void setResource(final String resource) {
        this.mResource = resource;
    }

    void setSource(final String source) {
        this.mSource = source;
    }

    void setWordCount(final int wordCount) {
        this.mWordCount = wordCount;
    }

    void setLanguage(final String language) {
        this.mLanguage = language;
    }

    void setIsSelected(final boolean isSelected) {
        this.mIsSelected = isSelected;
    }

    /**
     * Get the list name
     * @return the list name
     */
    String getListName() {
        return mListName;
    }

    /**
     * Get the list resource
     * @return the list resource
     */
    public String getResource() {
        return mResource;
    }

    /**
     * Get the list author
     * @param context the context
     * @return the list author
     */
    String getAuthor(final Context context) {
        final String resourceName = "library_" + mSource + "_author";
        final int resourceId = context.getResources()
                .getIdentifier(resourceName, "string", context.getPackageName());
        if (resourceId == 0) {
            return mSource;
        } else {
            return context.getString(resourceId);
        }
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

    /**
     * Get the list language, as ISO code
     * @return the list language
     */
    String getLanguage() {
        return mLanguage;
    }

    /**
     * Get the list language, as full text
     * @return the list language
     */
    String getLanguageText() {
        String language;
        String country;
        if (mLanguage.contains("_")) {
            language = mLanguage.split("_")[0];
            country = mLanguage.split("_")[1];
        } else {
            language = mLanguage;
            country = "";
        }
        final Locale locale = new Locale(language, country);
        return locale.getDisplayLanguage(locale);
    }

    boolean isSelected() {
        return mIsSelected;
    }

    @Override
    public String toString() {
        return "{" + mListName + ", " + mResource + ", " + mLanguage + ", " + mSource + ", "
                + mWordCount + ", " + mIsSelected + "}";
    }
}
