package com.marian;

public class PasswordHasher {
    private static final String salt="ad3c3ntsal7";

    public PasswordHasher() {
    }

    public String hash(String password){
        StringBuffer stringBuffer = new StringBuffer(password);
        String hashedString = String.valueOf(stringBuffer.toString().hashCode()) ;
        return hashedString+salt;

    }

}
