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
 * Chao, M. T. (1982) A general purpose unequal probability sampling plan. Biometrika, 69 (3): 653-656.
 * https://doi.org/10.1093/biomet/69.3.653
 * Vitter, Jeffrey S. (1 March 1985). "Random sampling with a reservoir". ACM Transactions on Mathematical
 * Software. 11 (1): 37–57. https://doi.org/10.1145/3147.3165
 * Wikipedia contributors. (2018, August 28). "Reservoir sampling". Wikipedia, The Free Encyclopedia. Retrieved
 * 16:56, September 10, 2018, from https://en.wikipedia.org/w/index.php?title=Reservoir_sampling&oldid=856932419
 */
public class ReservoirSampler {
    /**
     * A listener to receive progress updates on the sampling process.
     */
    private ReservoirUpdateListener mListener = null;

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
            return weightedSample(reservoirSize, inputStream, 1, Integer.MAX_VALUE);
        } else {
            return unweightedSample(reservoirSize, inputStream, 1, Integer.MAX_VALUE);
        }
    }

    /**
     * Use reservoir sampling to choose a random set of lines from an input stream of text.
     *
     * @param reservoirSize the number of lines to select
     * @param inputStream   the data stream to sample
     * @param weighted      flag indicating whether the sampling should be weighted or not
     * @param minLength     the minimum string length
     * @param maxLength     the maximum string length
     * @return a randomly chosen list of strings
     */
    public List<String> sample(final int reservoirSize, final InputStream inputStream, final boolean weighted,
                               final int minLength, final int maxLength) {
        if (weighted) {
            return weightedSample(reservoirSize, inputStream, minLength, maxLength);
        } else {
            return unweightedSample(reservoirSize, inputStream, minLength, maxLength);
        }
    }

    /**
     * Use unweighted reservoir sampling (Algorithm R)  to choose a random set of lines from an input stream of text.
     * This method assumes that the lines are in the format of "item to output\tweight of item", i.e. that it is
     * tab delimited, with the string's weight appearing after the string. Only the part of the line before the
     * first tab is used, with the string's weight discarded.
     *
     * @param reservoirSize the number of lines to select
     * @param inputStream   the data stream to sample
     * @param minLength     the minimum string length
     * @param maxLength     the maximum string length
     * @return a randomly chosen list of strings
     */
    private List<String> unweightedSample(final int reservoirSize, final InputStream inputStream, final int minLength,
                                          final int maxLength) {
        List<String> reservoirList = new ArrayList<>(reservoirSize);
        int count = 0;
        SecureRandom secureRandom = new SecureRandom();
        Scanner scanner = new Scanner(inputStream, "utf-8").useDelimiter("\n");
        while (scanner.hasNext()) {
            String currentLine = scanner.next();
            String currentWord = currentLine.split("\t")[0];
            if (currentWord.length() < minLength) {
                continue;
            }
            if (currentWord.length() > maxLength) {
                continue;
            }
            count++;
            if (count < reservoirSize) {
                reservoirList.add(currentLine);
            } else {
                int randomNumber = secureRandom.nextInt(count);
                if (randomNumber < reservoirSize) {
                    reservoirList.set(randomNumber, currentWord);
                    if (mListener != null) {
                        mListener.onReservoirUpdated(reservoirList);
                    }
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
     * @param minLength     the minimum string length
     * @param maxLength     the maximum string length
     * @return a randomly chosen list of strings
     */
    private List<String> weightedSample(final int reservoirSize, final InputStream inputStream, final int minLength,
                                        final int maxLength) {
        List<String> reservoirList = new ArrayList<>(reservoirSize);
        long count = 0;
        double totalWeight = 0;
        SecureRandom secureRandom = new SecureRandom();
        Scanner scanner = new Scanner(inputStream, "utf-8").useDelimiter("\n");
        while (scanner.hasNext()) {
            String currentLine = scanner.next();
            String[] splitLine = currentLine.split("\t");
            String currentWord = splitLine[0];
            if (currentWord.length() < minLength) {
                continue;
            }
            if (currentWord.length() > maxLength) {
                continue;
            }
            // if there is more than one element to splitLine, assume that the second element is the weight;
            // otherwise, use 1 as a default.
            int currentWeight = (splitLine.length > 1) ? Integer.getInteger(splitLine[1]) : 1;
            count++;
            totalWeight += currentWeight / count;
            if (count < reservoirSize) {
                reservoirList.add(currentLine);
            } else {
                double probability = currentWeight / totalWeight;
                double randomNumber = secureRandom.nextDouble();
                if (randomNumber <= probability) {
                    reservoirList.set(secureRandom.nextInt(reservoirSize), currentWord);
                    if (mListener != null) {
                        mListener.onReservoirUpdated(reservoirList);
                    }
                }
            }
        }
        scanner.close();
        return reservoirList;
    }

    /**
     * Interface to receive updates from the Sampler
     */
    public interface ReservoirUpdateListener {
        /**
         * Send an update to the listener, containing a partially filled reservoir.
         *
         * @param currentReservoir The current state of the reservoir. This should NOT be used as the final result
         *                         of the sampling, but allows the user to monitor the progress of the sampling.
         */
        void onReservoirUpdated(List<String> currentReservoir);
    }
}
