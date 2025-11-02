package com.creazione.space_learning.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

public class SecurityToken {

    @Value("${app.token.length:16}")
    private static int defaultTokenLength;

    public static String generateDefaultToken() {
        return generateBase64Token(defaultTokenLength);
    }

    public static String generateBase64Token(int byteLength) {
        byte[] bytes = new byte[byteLength];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    // Для Redis ключей сессий
    public static String generateSessionId() {
        return generateBase64Token(16);
    }

    // Для идентификаторов транзакций
    public static String generateTransactionId() {
        return generateBase64Token(8);
    }
}
