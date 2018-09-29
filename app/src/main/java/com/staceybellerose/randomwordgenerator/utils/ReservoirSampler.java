package com.staceybellerose.randomwordgenerator.utils;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class is used to randomly choose a sample of items from an input stream, where the length of the stream
 * is either very large or unknown, using Reservoir Sampling algorithms, both weighted and non-weighted. See
 * the following references for details:
 * Chao, M. T. (1982) "A general purpose unequal probability sampling plan". Biometrika, 69 (3): 653-656.
 * https://doi.org/10.1093/biomet/69.3.653
 * Grothaus, G. (2007, October 8). "Reservoir Sampling: Sampling from a stream of elements". Gregable.com.
 * Retrieved September 11, 2016, from from https://gregable.com/2007/10/reservoir-sampling.html
 * Knuth, D. E. (1981). The Art of Computer Programming. vol. 2: Seminumerical Algorithms (2nd ed).
 * Reading, MA: Addison-Wesley.
 * Vitter, Jeffrey S. (1 March 1985). "Random sampling with a reservoir". ACM Transactions on Mathematical
 * Software. 11 (1): 37–57. https://doi.org/10.1145/3147.3165
 * Wikipedia contributors. (2018, August 28). "Reservoir sampling". Wikipedia, The Free Encyclopedia. Retrieved
 * 16:56, September 10, 2018, from https://en.wikipedia.org/w/index.php?title=Reservoir_sampling&oldid=856932419
 */
@SuppressWarnings("unused")
public class ReservoirSampler {
    /**
     * A listener to receive progress updates on the sampling process.
     */
    private ReservoirUpdateListener mListener = null;
    /**
     * cryptographically secure random number generator
     */
    private SecureRandom mSecureRandom;
    /**
     * The minimum word length allowable
     */
    private int mMinLength = 0;
    /**
     * The maximum word length allowable
     */
    private int mMaxLength = Integer.MAX_VALUE;

    /**
     * Default constructor
     */
    public ReservoirSampler() {
        mSecureRandom = new SecureRandom();
    }

    public int getMinLength() {
        return mMinLength;
    }

    public void setMinLength(final int mMinLength) {
        this.mMinLength = mMinLength;
    }

    public int getMaxLength() {
        return mMaxLength;
    }

    public void setMaxLength(final int mMaxLength) {
        this.mMaxLength = mMaxLength;
    }

    /**
     * Set a listener to receive updates while the sampling process is running.
     *
     * @param listener The callback listener
     */
    public void setListener(final ReservoirUpdateListener listener) {
        mListener = listener;
    }

    /**
     * Use reservoir sampling to choose a random set of lines from an input stream of text.
     *
     * @param reservoirSize the number of lines to select
     * @param inputStream   the data stream to sample
     * @param weighted      flag indicating whether the sampling should be weighted or not
     * @return a randomly chosen list of strings
     */
    public List<String> sample(final int reservoirSize, final InputStream inputStream, final boolean weighted) {
        if (weighted) {
            return weightedSample(reservoirSize, inputStream);
        } else {
            return unweightedSample(reservoirSize, inputStream);
        }
    }

    /**
     * Use unweighted reservoir sampling (Algorithm R) to choose a random set of lines from an input stream of text.
     * This method assumes that the lines are in the format of "item to output\tweight of item", i.e. that it is
     * tab delimited, with the string's weight appearing after the string. Only the part of the line before the
     * first tab is used, with the string's weight discarded.
     *
     * @param reservoirSize the number of lines to select
     * @param inputStream   the data stream to sample
     * @return a randomly chosen list of strings
     */
    private List<String> unweightedSample(final int reservoirSize, final InputStream inputStream) {
        List<String> reservoirList = new ArrayList<>(reservoirSize);
        long count = 0;
        Scanner scanner = new Scanner(inputStream, "utf-8").useDelimiter("\n");
        while (scanner.hasNext()) {
            String currentWord = scanner.next().split("\t")[0];
            if (currentWord.length() < mMinLength || currentWord.length() > mMaxLength) {
                continue;
            }
            count++;
            if (count < reservoirSize) {
                reservoirList.add(currentWord);
            } else {
                long randomNumber = nextRandomLong(count);
                if (randomNumber < reservoirSize) {
                    reservoirList.set((int) randomNumber, currentWord);
                    updateListener(count, reservoirList);
                }
            }
        }
        scanner.close();
        return reservoirList;
    }

    /**
     * Use weighted reservoir sampling (Algorithm A-Chao) to choose a random set of lines from an input stream.
     * This method assumes that the lines are in the format of "item to output\tweight of item", i.e. that it is
     * tab delimited, with the string's weight appearing after the string.
     *
     * @param reservoirSize the number of lines to select
     * @param inputStream   the data stream to sample
     * @return a randomly chosen list of strings
     */
    private List<String> weightedSample(final int reservoirSize, final InputStream inputStream) {
        List<String> reservoirList = new ArrayList<>(reservoirSize);
        long count = 0;
        double totalWeight = 0;
        Scanner scanner = new Scanner(inputStream, "utf-8").useDelimiter("\n");
        while (scanner.hasNext()) {
            String[] splitLine = scanner.next().split("\t");
            String currentWord = splitLine[0];
            if (currentWord.length() < mMinLength || currentWord.length() > mMaxLength) {
                continue;
            }
            // if there is more than one element to splitLine, assume that the second element is the weight;
            // otherwise, use 1 as a default.
            int currentWeight = (splitLine.length > 1) ? Integer.getInteger(splitLine[1]) : 1;
            count++;
            totalWeight += currentWeight / count;
            if (count < reservoirSize) {
                reservoirList.add(currentWord);
            } else {
                double probability = currentWeight / totalWeight;
                double randomNumber = mSecureRandom.nextDouble();
                if (randomNumber <= probability) {
                    reservoirList.set(mSecureRandom.nextInt(reservoirSize), currentWord);
                    updateListener(count, reservoirList);
                }
            }
        }
        scanner.close();
        return reservoirList;
    }

    /**
     * Update the listener with the currently calculated list
     *
     * @param count the number of items checked
     * @param reservoirList the current reservoir
     */
    private void updateListener(final long count, final List<String> reservoirList) {
        if (mListener != null) {
            mListener.onReservoirUpdated(count, new ArrayList<>(reservoirList));
        }
    }

    /**
     * Returns a pseudo-random, uniformly distributed long value between 0 (inclusive) and the specified value
     * (exclusive), drawn from mSecureRandom's sequence.
     *
     * @param bound the upper bound (exclusive). Must be positive.
     * @return the next pseudo-random, uniformly distributed long value between zero (inclusive) and bound
     * (exclusive) from mSecureRandom's sequence.
     * @throws IllegalArgumentException if bound is not positive.
     */
    private long nextRandomLong(final long bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        if (bound < Integer.MAX_VALUE) {
            // since we need an int-sized random number, just return one
            return mSecureRandom.nextInt((int) bound);
        }
        long bits;
        long value;
        do {
            bits = (mSecureRandom.nextLong() << 1) >>> 1;
            value = bits % bound;
        } while (bits - value + (bound - 1) < 0L);
        return value;
    }

    /**
     * Interface to receive updates from the Sampler
     */
    @SuppressWarnings("WeakerAccess")
    public interface ReservoirUpdateListener {
        /**
         * Send an update to the listener, containing a partially filled reservoir.
         *
         * @param count         The number of items that have been processed.
         * @param currentSample The current state of the reservoir. This should NOT be used as the final result
         *                      of the sampling, but allows the user to monitor the progress of the sampling.
         */
        void onReservoirUpdated(long count, List<String> currentSample);
    }
}
