package ru.maverick.cardmanagementsystem.utils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class KeyGen {
    public static void main(String[] args) {
        String secretkey = "";

            try {
                KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
                SecretKey sk = keyGen.generateKey();
                secretkey = Base64.getEncoder().encodeToString(sk.getEncoded());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

        System.out.println(secretkey);
    }
}
