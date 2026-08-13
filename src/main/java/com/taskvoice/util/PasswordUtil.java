package com.taskvoice.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private static final int LOG_ROUNDS = 12;

    private PasswordUtil() {}

    /** Hash a plaintext password. */
    public static String hash(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(LOG_ROUNDS));
    }

    /** Verify a plaintext password against a stored hash. */
    public static boolean verify(String plaintext, String hash) {
        if (hash == null || hash.isBlank()) return false;
        try {
            return BCrypt.checkpw(plaintext, hash);
        } catch (Exception e) {
            return false;
        }
    }
}
