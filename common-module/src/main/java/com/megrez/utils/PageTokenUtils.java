package com.megrez.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密对象的通用工具类
 */
public class PageTokenUtils {

    // 密钥
    private static final String AES_KEY = "1234567890abcdef";

    // 加密
    public static <T> String encryptState(T state) throws Exception {
        String json = JSONUtils.toJSON(state);
        byte[] keyBytes = AES_KEY.getBytes();
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));

        return Base64.getUrlEncoder().encodeToString(encrypted);
    }

    // 解密
    public static <T> T decryptState(String token, Class<T> clazz) throws Exception {
        byte[] encryptedBytes = Base64.getUrlDecoder().decode(token);
        SecretKey key = new SecretKeySpec(AES_KEY.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(encryptedBytes);

        return JSONUtils.fromJSON(new String(decrypted, StandardCharsets.UTF_8), clazz);

    }
}
