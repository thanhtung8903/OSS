package com.example.oss.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtils {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    /**
     * Hash password using SHA-256
     * 
     * @param password Plain text password
     * @return Hashed password string
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Hash password with salt (more secure)
     * 
     * @param password Plain text password
     * @param salt     Salt bytes
     * @return Hashed password string
     */
    public static String hashPasswordWithSalt(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password with salt", e);
        }
    }

    /**
     * Generate random salt
     * 
     * @return Random salt bytes
     */
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Convert salt bytes to Base64 string for storage
     * 
     * @param salt Salt bytes
     * @return Base64 encoded salt string
     */
    public static String saltToString(byte[] salt) {
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Convert Base64 salt string back to bytes
     * 
     * @param saltString Base64 encoded salt string
     * @return Salt bytes
     */
    public static byte[] saltFromString(String saltString) {
        return Base64.getDecoder().decode(saltString);
    }

    /**
     * Verify password against hash
     * 
     * @param password       Plain text password
     * @param hashedPassword Stored hashed password
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        String inputHash = hashPassword(password);
        return inputHash.equals(hashedPassword);
    }

    /**
     * Verify password against hash with salt
     * 
     * @param password       Plain text password
     * @param hashedPassword Stored hashed password
     * @param salt           Salt used for hashing
     * @return true if password matches
     */
    public static boolean verifyPasswordWithSalt(String password, String hashedPassword, byte[] salt) {
        String inputHash = hashPasswordWithSalt(password, salt);
        return inputHash.equals(hashedPassword);
    }

    /**
     * Check password strength
     * 
     * @param password Password to check
     * @return true if password meets minimum requirements
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpper = true;
            else if (Character.isLowerCase(c))
                hasLower = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            else if (!Character.isLetterOrDigit(c))
                hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Generate secure random token for password reset, etc.
     * 
     * @param length Token length
     * @return Random token string
     */
    public static String generateSecureToken(int length) {
        SecureRandom random = new SecureRandom();
        byte[] token = new byte[length];
        random.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    /**
     * Validate email format
     * 
     * @param email Email to validate
     * @return true if email format is valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }
}