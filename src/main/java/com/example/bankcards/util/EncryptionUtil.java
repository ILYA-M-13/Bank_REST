package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Component
public class EncryptionUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding"; // Изменено на CBC

    @Value("${bankcards.app.encryptionKey}")
    private String encryptionKey;

    private SecretKeySpec getValidKey() {
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);

        byte[] validKeyBytes = Arrays.copyOf(keyBytes, 32); // Используем 256-битный ключ
        return new SecretKeySpec(validKeyBytes, ALGORITHM);
    }

    public String encrypt(String data) {
        try {
            SecretKeySpec secretKey = getValidKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            byte[] iv = new byte[16];
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec secretKey = getValidKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            byte[] iv = new byte[16];
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}