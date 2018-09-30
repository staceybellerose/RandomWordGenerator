package com.staceybellerose.randomwordgenerator.utils;

import android.annotation.SuppressLint;

import java.util.HashMap;

/**
 * Utility class to manage a simplified mapping that allows multiple data types in values added.
 * Does not implement all methods required for a Map, or manage all data types. Currently handles
 * Strings, Integers, and Booleans.
 */
@SuppressWarnings("WeakerAccess")
@SuppressLint("UseSparseArrays")
public class TypedMap {
    /**
     * Internal HashMap to manage Strings
     */
    private HashMap<Integer, String> mStringMap;
    /**
     * Internal HashMap to manage Integers
     */
    private HashMap<Integer, Integer> mIntegerMap;
    /**
     * Internal HashMap to manage Booleans
     */
    private HashMap<Integer, Boolean> mBooleanMap;

    /**
     * Constructor
     */
    public TypedMap() {
        mStringMap = new HashMap<>();
        mIntegerMap = new HashMap<>();
        mBooleanMap = new HashMap<>();
    }

    /**
     * Associates the specified value with the specified key in this map. If the map previously
     * contained a mapping for the key, the old value is replaced.
     *
     * @param resourceId key with which the specified value is to be associated
     * @param contents value to be associated with the specified key
     */
    public void put(final int resourceId, final String contents) {
        mStringMap.put(resourceId, contents);
    }

    /**
     * Associates the specified value with the specified key in this map. If the map previously
     * contained a mapping for the key, the old value is replaced.
     *
     * @param resourceId key with which the specified value is to be associated
     * @param contents value to be associated with the specified key
     */
    public void put(final int resourceId, final int contents) {
        mIntegerMap.put(resourceId, contents);
    }

    /**
     * Associates the specified value with the specified key in this map. If the map previously
     * contained a mapping for the key, the old value is replaced.
     *
     * @param resourceId key with which the specified value is to be associated
     * @param contents value to be associated with the specified key
     */
    public void put(final int resourceId, final boolean contents) {
        mBooleanMap.put(resourceId, contents);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no
     * mapping for the key.
     *
     * @param resourceId the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if this map contains
     * no mapping for the key
     */
    public String getString(final int resourceId) {
        return mStringMap.get(resourceId);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no
     * mapping for the key.
     *
     * @param resourceId the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if this map contains
     * no mapping for the key
     */
    public int getInteger(final int resourceId) {
        return mIntegerMap.get(resourceId);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no
     * mapping for the key.
     *
     * @param resourceId the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if this map contains
     * no mapping for the key
     */
    public boolean getBoolean(final int resourceId) {
        return mBooleanMap.get(resourceId);
    }

}
