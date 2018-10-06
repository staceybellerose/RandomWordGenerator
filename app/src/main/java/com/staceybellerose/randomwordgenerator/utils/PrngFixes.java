package com.staceybellerose.randomwordgenerator.utils;

/*
 * See https://android-developers.googleblog.com/2013/08/some-securerandom-thoughts.html
 * for details why this is necessary.
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will Google be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, as long as the origin is not misrepresented.
 */

import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import java.security.Security;

/**
 * Fixes for the output of the default PRNG having low entropy.
 *
 * The fixes need to be applied via {@link #apply()} before any use of Java
 * Cryptography Architecture primitives. A good place to invoke them is in the
 * application's {@code onCreate}.
 */
public final class PrngFixes {

    /**
     * Jelly Bean version code
     */
    private static final int VERSION_CODE_JELLY_BEAN = 16;
    /**
     * Jelly Bean MR2 version code
     */
    private static final int VERSION_CODE_JELLY_BEAN_MR2 = 18;
    /**
     * build fingerprint and device serial number
     */
    private static final byte[] BUILD_FINGERPRINT_AND_DEVICE_SERIAL = getBuildFingerprintAndDeviceSerial();
    /**
     * Buffer size to use when reading from URANDOM
     */
    private static final int URANDOM_BUFFER_SIZE = 1024;

    /** Hidden constructor to prevent instantiation. */
    private PrngFixes() { }

    /**
     * Applies all fixes.
     *
     * @throws SecurityException if a fix is needed but could not be applied.
     */
    public static void apply() {
        applyOpenSSLFix();
        installLinuxPRNGSecureRandom();
    }

    /**
     * Applies the fix for OpenSSL PRNG having low entropy. Does nothing if the
     * fix is not needed.
     *
     * @throws SecurityException if the fix is needed but could not be applied.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private static void applyOpenSSLFix() throws SecurityException {
        if ((Build.VERSION.SDK_INT < VERSION_CODE_JELLY_BEAN)
                || (Build.VERSION.SDK_INT > VERSION_CODE_JELLY_BEAN_MR2)) {
            // No need to apply the fix
            return;
        }

        try {
            // Mix in the device- and invocation-specific seed.
            //noinspection PrimitiveArrayArgumentToVarargsMethod
            Class.forName("org.apache.harmony.xnet.provider.jsse.NativeCrypto")
                    .getMethod("RAND_seed", byte[].class)
                    .invoke(null, generateSeed());

            // Mix output of Linux PRNG into OpenSSL's PRNG
            final int bytesRead = (Integer) Class.forName(
                    "org.apache.harmony.xnet.provider.jsse.NativeCrypto")
                    .getMethod("RAND_load_file", String.class, long.class)
                    .invoke(null, "/dev/urandom", URANDOM_BUFFER_SIZE);
            if (bytesRead != URANDOM_BUFFER_SIZE) {
                throw new IOException(
                        "Unexpected number of bytes read from Linux PRNG: "
                                + bytesRead);
            }
        } catch (Exception e) {
            throw new SecurityException("Failed to seed OpenSSL PRNG", e);
        }
    }

    /**
     * Installs a Linux PRNG-backed {@code SecureRandom} implementation as the
     * default. Does nothing if the implementation is already the default or if
     * there is not need to install the implementation.
     *
     * @throws SecurityException if the fix is needed but could not be applied.
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static void installLinuxPRNGSecureRandom() throws SecurityException {
        if (Build.VERSION.SDK_INT > VERSION_CODE_JELLY_BEAN_MR2) {
            // No need to apply the fix
            return;
        }

        // Install a Linux PRNG-based SecureRandom implementation as the default, if not yet installed.
        final Provider[] secureRandomProviders = Security.getProviders("SecureRandom.SHA1PRNG");
        if ((secureRandomProviders == null) || (secureRandomProviders.length < 1)
                || (!LinuxPRNGSecureRandomProvider.class.equals(secureRandomProviders[0].getClass()))) {
            Security.insertProviderAt(new LinuxPRNGSecureRandomProvider(), 1);
        }

        // Assert that new SecureRandom() and SecureRandom.getInstance("SHA1PRNG") return a SecureRandom backed
        // by the Linux PRNG-based SecureRandom implementation.
        final SecureRandom rng1 = new SecureRandom();
        if (!LinuxPRNGSecureRandomProvider.class.equals(rng1.getProvider().getClass())) {
            throw new SecurityException("new SecureRandom() backed by wrong Provider: "
                    + rng1.getProvider().getClass());
        }

        SecureRandom rng2;
        try {
            rng2 = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("SHA1PRNG not available", e);
        }
        if (!LinuxPRNGSecureRandomProvider.class.equals(rng2.getProvider().getClass())) {
            throw new SecurityException("SecureRandom.getInstance(\"SHA1PRNG\") backed by wrong"
                    + " Provider: " + rng2.getProvider().getClass());
        }
    }

    /**
     * Generates a device- and invocation-specific seed to be mixed into the
     * Linux PRNG.
     *
     * @return generated seed
     */
    private static byte[] generateSeed() {
        try {
            final ByteArrayOutputStream seedBuffer = new ByteArrayOutputStream();
            final DataOutputStream seedBufferOut = new DataOutputStream(seedBuffer);
            seedBufferOut.writeLong(System.currentTimeMillis());
            seedBufferOut.writeLong(System.nanoTime());
            seedBufferOut.writeInt(Process.myPid());
            seedBufferOut.writeInt(Process.myUid());
            seedBufferOut.write(BUILD_FINGERPRINT_AND_DEVICE_SERIAL);
            seedBufferOut.close();
            return seedBuffer.toByteArray();
        } catch (IOException e) {
            throw new SecurityException("Failed to generate seed", e);
        }
    }

    /**
     * Gets the hardware serial number of this device.
     *
     * @return serial number or {@code null} if not available.
     */
    private static String getDeviceSerialNumber() {
        // We're using the Reflection API because Build.SERIAL is only available
        // since API Level 9 (Gingerbread, Android 2.3).
        //noinspection TryWithIdenticalCatches
        try {
            return (String) Build.class.getField("SERIAL").get(null);
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException ignored) {
        }
        return null;
    }

    /**
     * Get build fingerprint and device serial number as byte[]
     *
     * @return byte[] containing build fingerprint and device serial number
     */
    private static byte[] getBuildFingerprintAndDeviceSerial() {
        final StringBuilder result = new StringBuilder();
        final String fingerprint = Build.FINGERPRINT;
        if (fingerprint != null) {
            result.append(fingerprint);
        }
        final String serial = getDeviceSerialNumber();
        if (serial != null) {
            result.append(serial);
        }
        try {
            return result.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 encoding not supported", e);
        }
    }

    /**
     * {@code Provider} of {@code SecureRandom} engines which pass through
     * all requests to the Linux PRNG.
     */
    @SuppressWarnings("PMD.FieldNamingConventions")
    private static class LinuxPRNGSecureRandomProvider extends Provider {

        /**
         * Unique ID used in Serializable
         */
        private static final long serialVersionUID = 314159265359L;
        /**
         * constructor
         */
        LinuxPRNGSecureRandomProvider() {
            super("LinuxPRNG",
                    1.0,
                    "A Linux-specific random number provider that uses"
                            + " /dev/urandom");
            // Although /dev/urandom is not a SHA-1 PRNG, some apps
            // explicitly request a SHA1PRNG SecureRandom and we thus need to
            // prevent them from getting the default implementation whose output
            // may have low entropy.
            put("SecureRandom.SHA1PRNG", LinuxPRNGSecureRandom.class.getName());
            put("SecureRandom.SHA1PRNG ImplementedIn", "Software");
        }
    }

    /**
     * {@link SecureRandomSpi} which passes all requests to the Linux PRNG
     * ({@code /dev/urandom}).
     */
    @SuppressWarnings("PMD.FieldNamingConventions")
    private static class LinuxPRNGSecureRandom extends SecureRandomSpi {

        /*
         * IMPLEMENTATION NOTE: Requests to generate bytes and to mix in a seed
         * are passed through to the Linux PRNG (/dev/urandom). Instances of
         * this class seed themselves by mixing in the current time, PID, UID,
         * build fingerprint, and hardware serial number (where available) into
         * Linux PRNG.
         *
         * Concurrency: Read requests to the underlying Linux PRNG are
         * serialized (on S_LOCK) to ensure that multiple threads do not get
         * duplicated PRNG output.
         */

        /**
         * Unique ID used in Serializable
         */
        private static final long serialVersionUID = 271828182846L;
        /**
         * Linux PRNG file
         */
        private static final File URANDOM_FILE = new File("/dev/urandom");
        /**
         * synchronization lock object
         */
        private static final Object S_LOCK = new Object();

        /**
         * Input stream for reading from Linux PRNG or {@code null} if not yet
         * opened.
         *
         * @GuardedBy("S_LOCK")
         */
        private static DataInputStream sUrandomIn;

        /**
         * Output stream for writing to Linux PRNG or {@code null} if not yet
         * opened.
         *
         * @GuardedBy("S_LOCK")
         */
        private static OutputStream sUrandomOut;

        /**
         * Whether this engine instance has been seeded. This is needed because
         * each instance needs to seed itself if the client does not explicitly
         * seed it.
         */
        private boolean mSeeded;

        @Override
        protected void engineSetSeed(final byte[] bytes) {
            try {
                OutputStream out;
                synchronized (S_LOCK) {
                    out = getUrandomOutputStream();
                }
                out.write(bytes);
                out.flush();
            } catch (IOException e) {
                // On a small fraction of devices /dev/urandom is not writable.
                // Log and ignore.
                Log.w(PrngFixes.class.getSimpleName(),
                        "Failed to mix seed into " + URANDOM_FILE);
            } finally {
                mSeeded = true;
            }
        }

        @Override
        protected void engineNextBytes(final byte[] bytes) {
            if (!mSeeded) {
                // Mix in the device- and invocation-specific seed.
                engineSetSeed(generateSeed());
            }

            try {
                DataInputStream inputStream;
                synchronized (S_LOCK) {
                    inputStream = getUrandomInputStream();
                }
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (inputStream) {
                    inputStream.readFully(bytes);
                }
            } catch (IOException e) {
                throw new SecurityException(
                        "Failed to read from " + URANDOM_FILE, e);
            }
        }

        @Override
        protected byte[] engineGenerateSeed(final int size) {
            final byte[] seed = new byte[size];
            engineNextBytes(seed);
            return seed;
        }

        /**
         * Get a Data Input Stream for /dev/urandom
         *
         * @return data input stream
         */
        private DataInputStream getUrandomInputStream() {
            synchronized (S_LOCK) {
                if (sUrandomIn == null) {
                    // NOTE: Consider inserting a BufferedInputStream between
                    // DataInputStream and FileInputStream if you need higher
                    // PRNG output performance and can live with future PRNG
                    // output being pulled into this process prematurely.
                    try {
                        sUrandomIn = new DataInputStream(
                                new FileInputStream(URANDOM_FILE));
                    } catch (IOException e) {
                        throw new SecurityException("Failed to open "
                                + URANDOM_FILE + " for reading", e);
                    }
                }
                return sUrandomIn;
            }
        }

        /**
         * Get an Output Stream for /dev/urandom
         *
         * @return output stream
         * @throws IOException if IO errors occur
         */
        private OutputStream getUrandomOutputStream() throws IOException {
            synchronized (S_LOCK) {
                if (sUrandomOut == null) {
                    sUrandomOut = new FileOutputStream(URANDOM_FILE);
                }
                return sUrandomOut;
            }
        }
    }
}