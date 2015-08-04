package com.zva.android.commonLib.utils;

import java.security.SecureRandom;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 01/08/15.
 */
public class StringUtils {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Gets a uuid of specified length
     *
     * @param length the number of characters in the unique identifier
     * @return the uuid
     */
    @Contract(pure = true)
    @NotNull
    public static String getUuid(int length) {

        String encodingDict = "ABCDEFGHJIKLMONPQRTSUVWXYZ1234567890";

        StringBuilder builder = new StringBuilder();

        SecureRandom secureRandom = new SecureRandom();

        int dictionaryLength = encodingDict.length();

        for (int i = 0; i < length; i++)
            builder.append(encodingDict.charAt(secureRandom.nextInt(dictionaryLength)));

        return builder.toString();

    }

    @Contract(pure = true)
    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Contract(pure = true)
    public static String distributeSubstringAcrossString(String substring, String string, int interval) {
        StringBuilder returnStringBuilder = new StringBuilder();
        for (int i = 0; i < string.length() / interval; i++)
            returnStringBuilder.append(string.substring(i * interval, (i + 1) * interval)).append(substring);

        return returnStringBuilder.substring(0, returnStringBuilder.length() - substring.length());
    }

    @Contract(pure = true)
    public static boolean isAnyEmpty(String... strings) {

        for (String string : strings)
            if (isEmpty(string))
                return true;

        return false;

    }

    @Contract(value = "null -> true", pure = true)
    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    @Contract(value = "!null, _, _->!null; null, _, _->null", pure = true)
    public static String replace(String text, String searchString, String replacement) {
        return org.apache.commons.lang3.StringUtils.replace(text, searchString, replacement);
    }

    @Contract(value = "null -> null", pure = true)
    public static String capitalize(String stringToCapitalize) {

        if (isEmpty(stringToCapitalize)) {
            return stringToCapitalize;
        }

        char[] arr = stringToCapitalize.toCharArray();

        boolean capitalizeNext = true;

        String phrase = "";

        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }

        return phrase;
    }

    @Contract("null, _->null; !null, !null ->!null; !null, null -> null")
    public static String join(String[] parts, String joiningString) {

        StringBuilder joinedString = new StringBuilder();

        if (parts == null)
            return null;

        if (joiningString == null)
            return null;

        for (String part : parts)
            joinedString.append(part).append(joiningString);

        return joinedString.toString();

    }

    public static String getBasePackageName(Class<?> candidateClass) {
        String candidateClassName = candidateClass.getName();

        return candidateClassName.substring(0, candidateClassName.lastIndexOf("."));
    }
}
