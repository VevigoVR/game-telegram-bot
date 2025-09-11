package com.creazione.space_learning.config;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
public class ReferralCodec {
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final int USER_ID_BYTES = 6; // 48 бит для ID (до 281 триллиона)
    private static final int HMAC_BYTES = 4;    // 32 бита для проверки
    private static final int TOTAL_BYTES = USER_ID_BYTES + HMAC_BYTES;

    private final byte[] secretKey;
    private final Mac hmac;

    public ReferralCodec(String base64Key) throws NoSuchAlgorithmException, InvalidKeyException {
        this.secretKey = Base64.getDecoder().decode(base64Key);
        this.hmac = Mac.getInstance(HMAC_ALGO);
        this.hmac.init(new SecretKeySpec(secretKey, HMAC_ALGO));
    }

    public String encodeUserId(long userId) {
        byte[] userIdBytes = longToBytes(userId, USER_ID_BYTES);
        byte[] hmacBytes = hmac.doFinal(userIdBytes);
        byte[] truncatedHmac = new byte[HMAC_BYTES];
        System.arraycopy(hmacBytes, 0, truncatedHmac, 0, HMAC_BYTES);

        byte[] combined = new byte[TOTAL_BYTES];
        System.arraycopy(userIdBytes, 0, combined, 0, USER_ID_BYTES);
        System.arraycopy(truncatedHmac, 0, combined, USER_ID_BYTES, HMAC_BYTES);

        return Base36.encode(combined);
    }

    public long decodeUserId(String code) {
        byte[] combined = Base36.decode(code);

        // Проверяем минимальную длину
        if (combined.length < TOTAL_BYTES) {
            // Дополняем нулями в начале
            byte[] padded = new byte[TOTAL_BYTES];
            int offset = TOTAL_BYTES - combined.length;
            System.arraycopy(combined, 0, padded, offset, combined.length);
            combined = padded;
        } else if (combined.length > TOTAL_BYTES) {
            // Обрезаем лишние байты в начале
            combined = Arrays.copyOfRange(combined, combined.length - TOTAL_BYTES, combined.length);
        }

        byte[] userIdBytes = new byte[USER_ID_BYTES];
        byte[] codeHmac = new byte[HMAC_BYTES];
        System.arraycopy(combined, 0, userIdBytes, 0, USER_ID_BYTES);
        System.arraycopy(combined, USER_ID_BYTES, codeHmac, 0, HMAC_BYTES);

        byte[] calculatedHmac = hmac.doFinal(userIdBytes);
        byte[] truncatedCalculatedHmac = new byte[HMAC_BYTES];
        System.arraycopy(calculatedHmac, 0, truncatedCalculatedHmac, 0, HMAC_BYTES);

        if (!MessageDigest.isEqual(codeHmac, truncatedCalculatedHmac)) {
            //throw new SecurityException("HMAC verification failed");
            log.info("HMAC verification failed (was early at ReferralCodec.java:65)");
        }

        return bytesToLong(userIdBytes);
    }

    private byte[] longToBytes(long value, int bytes) {
        byte[] result = new byte[bytes];
        for (int i = bytes - 1; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }

    private long bytesToLong(byte[] bytes) {
        long result = 0;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }
}