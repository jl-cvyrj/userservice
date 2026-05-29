package com.innowise.userservice.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    private static final int BCRYPT_COST = 12;
    private static final String BCRYPT_PREFIX_2A = "$2a$";
    private static final String BCRYPT_PREFIX_2B = "$2b$";
    private static final String BCRYPT_PREFIX_2Y = "$2y$";

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }
        if (!isHashedPassword(hashedPassword)) {
            return false;
        }
        return BCrypt.checkpw(password, hashedPassword);
    }

    public static boolean isHashedPassword(String password) {
        if (password == null) return false;
        return password.startsWith(BCRYPT_PREFIX_2A) || password.startsWith(BCRYPT_PREFIX_2B) || password.startsWith(BCRYPT_PREFIX_2Y);
    }
}