package com.staceybellerose.randomwordgenerator.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to manage a simplified mapping that allows multiple data types in values added.
 * Does not implement all methods required for a Map, or manage all data types. Currently handles
 * Strings, Integers, and Booleans.
 */
class TypedMap {
    /**
     * Internal HashMap to manage Strings
     */
    private final Map<Integer, String> mStringMap;
    /**
     * Internal HashMap to manage Integers
     */
    private final Map<Integer, Integer> mIntegerMap;
    /**
     * Internal HashMap to manage Booleans
     */
    private final Map<Integer, Boolean> mBooleanMap;

    /**
     * Constructor
     */
    TypedMap() {
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
    void put(final int resourceId, final String contents) {
        mStringMap.put(resourceId, contents);
    }

    /**
     * Associates the specified value with the specified key in this map. If the map previously
     * contained a mapping for the key, the old value is replaced.
     *
     * @param resourceId key with which the specified value is to be associated
     * @param contents value to be associated with the specified key
     */
    void put(final int resourceId, final int contents) {
        mIntegerMap.put(resourceId, contents);
    }

    /**
     * Associates the specified value with the specified key in this map. If the map previously
     * contained a mapping for the key, the old value is replaced.
     *
     * @param resourceId key with which the specified value is to be associated
     * @param contents value to be associated with the specified key
     */
    void put(final int resourceId, final boolean contents) {
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
    String getString(final int resourceId) {
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
    int getInteger(final int resourceId) {
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
    boolean getBoolean(final int resourceId) {
        return mBooleanMap.get(resourceId);
    }

}
