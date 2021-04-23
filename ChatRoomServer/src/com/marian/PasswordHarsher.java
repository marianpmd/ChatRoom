package com.marian;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Clasa care realizaeaza criptarea parolelor
 */
public class PasswordHarsher {
    /**
     * Salt ultilizat in criptarea parolelor , ideal este unic fiecatui utilizator si salvat separat
     */
    private static final String salt="ad3c3ntsal7";

    public PasswordHarsher() {
    }

    /**
     * Realizaeaza criptarea parolei date ca si parametru
     *
     * @param password parola
     * @return parola criptata cu SHA-256
     */
    public String hashWithSalt(String password){
        String hashedPassword=hash(password);
        return hash(hashedPassword + salt);

    }

    /**
     * Realizeaza criptarea efectiva cu SHA-256 , fara salt a parolei date ca si parametru
     *
     * @param password parola
     * @return parola criptata(String)
     */
    private String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
