package com.zva.android.commonLib.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 01/08/15.
 */
public class CipherUtils {
    private static final String DEFAULT_DIGEST_ALGORITHM = "SHA-512";

    /**
     * Converts bytes to a hex string digest
     *
     * @param originalMessage the original message
     * @return the string
     */
    public static String hexStringDigest(byte[] originalMessage) {
        String bytes;
        try {
            bytes = StringUtils.bytesToHexString(digestMessage(originalMessage, DEFAULT_DIGEST_ALGORITHM));
        } catch (NoSuchAlgorithmException ignored) {
            throw new IllegalStateException("Default digest algorithm not found");
        }

        return bytes;
    }

    /**
     * Digests the given message with the default digest algorithm ({@link CipherUtils#DEFAULT_DIGEST_ALGORITHM})
     *
     * @param originalMessage the original message
     * @return the bytes of the digested message
     */
    public static byte[] digestMessage(byte[] originalMessage) {

        byte[] bytes;

        try {
            bytes = digestMessage(originalMessage, DEFAULT_DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException ignored) {
            throw new IllegalStateException("Default digest algorithm not found");
        }

        return bytes;
    }

    /**
     * Digests the given message with the given algorithm
     *
     * @param originalMessage the original message
     * @param algorithm the algorithm
     * @return the byte [ ]
     * @throws NoSuchAlgorithmException if the given algorithm does not exist in JCA
     */
    public static byte[] digestMessage(byte[] originalMessage, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest messageDigest;
        messageDigest = MessageDigest.getInstance(algorithm);

        messageDigest.update(originalMessage, 0, originalMessage.length);
        return messageDigest.digest();
    }

}
