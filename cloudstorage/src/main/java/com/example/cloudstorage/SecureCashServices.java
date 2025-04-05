package com.example.cloudstorage;

import android.os.Build;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SecureCashServices {
    private static SecretKey key;
    private static IvParameterSpec iv = new IvParameterSpec(new byte[] {0, 17, 56, -30, 50, 127, -127, 37, 25, 44, 59, 18, 55, 11, 79, 100});

    public static SecretKey getKeyFromPassword(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 6556, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(), "AES");
        return secret;
    }

    public static byte[] encrypt(byte[] input) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        String algorithm = "AES/CBC/PKCS5Padding";
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return cipherText;
        }
        return null;
    }
    public static byte[] decrypt(byte[] input) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        String algorithm = "AES/CBC/PKCS5Padding";
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return cipherText;
        }
        return null;
    }
    public static void Init() throws NoSuchAlgorithmException, InvalidKeySpecException {
        key = getKeyFromPassword("ochen' slozhni parol pzh ne vzlamivaite duri", "vhuhlgvjjvvlvjhvjhvjhvjhjvhvljvsf.dsfsnfdms,nf.nfdsmn");
    }
}
