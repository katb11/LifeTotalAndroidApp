package com.example.lifetotal.Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {

    private static final String secretKey = "c7FZP8n5c7FZP8n5";

    public static byte[] encrypt(byte[] clearText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(),"AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(clearText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
