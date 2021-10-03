package com.staceybellerose.randomwordgenerator.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Hack to fix known memory leak in Android SDK. Taken from https://stackoverflow.com/a/30397901
 */
public final class SdkLeakFixer {
    /**
     * sCached field of TextLine class
     */
    private static final Field TEXT_LINE_CACHED;

    static {
        Field textLineCached = null;
        try {
            textLineCached = Class.forName("android.text.TextLine").getDeclaredField("sCached");
            textLineCached.setAccessible(true);
        } catch (ClassNotFoundException ignored) {
        } catch (NoSuchFieldException ignored) {
        }
        TEXT_LINE_CACHED = textLineCached;
    }

    /**
     * Private Constructor
     */
    private SdkLeakFixer() {
    }

    /**
     * Hack to fix an SDK memory leak in android.text.TextLine
     */
    public static void clearTextLineCache() {
        // If the field was not found for whatever reason just return.
        if (TEXT_LINE_CACHED == null) {
            return;
        }

        Object cached = null;
        try {
            // Get reference to the TextLine sCached array.
            cached = TEXT_LINE_CACHED.get(null);
        } catch (IllegalAccessException ignored) {
        }
        if (cached != null) {
            // Clear the array.
            final int size = Array.getLength(cached);
            for (int i = 0; i < size; i++) {
                Array.set(cached, i, null);
            }
        }
    }
}
