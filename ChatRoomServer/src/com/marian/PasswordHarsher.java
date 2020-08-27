package com.marian;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class PasswordHarsher {
    private static final String salt="ad3c3ntsal7";

    public PasswordHarsher() {
    }

    public String hash(String password){

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            /*String beforeSalt = Arrays.toString(hash);*/
            StringBuilder sb= new StringBuilder(Arrays.toString(hash));
            sb.append(salt);
            byte[]reHash= digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return Arrays.toString(reHash);

        } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        }
        return null;
    }

}
