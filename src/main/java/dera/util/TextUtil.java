package dera.util;

import java.io.InputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Scanner;

public final class TextUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String ZERO_NUMERIC = "0123456789";
    private static final String NONZERO_NUMERIC = "123456789";
    private static final String HTTPS_REGEX = "(^https://)(.*)";
    private static final int RANDOM_FIXED_LENGTH = 10;


    private TextUtil() {
    }

    public static boolean isSecureUri(String uri) {
        return uri != null && uri.matches(HTTPS_REGEX);
    }

    public static boolean isSecureUri(URI uri) {
        return uri != null && uri.toString().matches(HTTPS_REGEX);
    }

    public static String convert(java.io.InputStream inputStream) {
        return convert(inputStream, "UTF-8");
    }

    public static String convert(java.io.InputStream inputStream, String charsetName) {
        if (inputStream != null) {
            Scanner scanner = new Scanner(inputStream, charsetName != null ? charsetName : "UTF-8").useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
        return null;
    }

    public static boolean nullOrEmpty(String s) {
        return (s == null || s.isEmpty());
    }

    public static boolean neitherNullNorEmpty(String s) {
        return !nullOrEmpty(s);
    }

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public static String randomFixedLengthString() {
        return randomString(RANDOM_FIXED_LENGTH);
    }


    public static String randomNumber(int len) {
        StringBuilder sb = new StringBuilder(len);
        sb.append(NONZERO_NUMERIC.charAt(RANDOM.nextInt(NONZERO_NUMERIC.length())));
        for (int i = 0; i < len - 1; i++) {
            sb.append(ZERO_NUMERIC.charAt(RANDOM.nextInt(ZERO_NUMERIC.length())));
        }
        return sb.toString();
    }

    public static String randomFixedLengthNumber() {
        return randomNumber(RANDOM_FIXED_LENGTH);
    }

    public static String randomId(String prefix) {
        StringBuffer id = new StringBuffer(prefix);
        id.append("-").append(randomFixedLengthString());
        return id.toString();
    }

    public static String convertStreamToString(InputStream is, String charset) {
        Scanner s = new Scanner(is, charset).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}