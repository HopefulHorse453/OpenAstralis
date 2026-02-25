package club.serenityutils.utils;

import io.github.kawase.NativeObfuscate;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class EncryptionUtil {
    private static final int KEY_SIZE = 32; // 256-bit key

    @NativeObfuscate
    public static byte[] generateDynamicKey(long seed) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = Base64.getEncoder().encodeToString(cc.astralis.protection.util.Base64.decode("r07JdQhUpCHIhfNns8R4TKIOEtBng3wI1J8uRrFUPuA="))
                    + "|" + seed;
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(hash, KEY_SIZE);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate dynamic key", e);
        }
    }

    public static final PrivateKey PRIVATE_KEY;

    static {
        final String privateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDbM61tJIcXGjmWHlTPzJqzWQbdUljSLByUgHkRkgv3welUJ2HfecQlNicntCs62vTrcH1jZc8WL/aVroPtiNIYdhvzgG4k706qoxv3V+s3qfk7wWS+6h30b/BhAoMhxIi1VoVFkcNZRo1K5WhE0EKens0ToZXjaKnkAGNdnlp22QyQ8I4hQm3S7CPMv1r1us/hIlZzrJvDJLtx2aarfG+ygwyA+aX2LcU6i+Z9f9i01pjQWBCb4gmBpu+TJ1NYcH/T1/cO1QB1QDHPDd2gjepe22w53tPkUjqN0uNKJakAEnTAXhUx3GE98dZ+aDpYcpJSqtJF9Om30CUd/2Gtc1lZAgMBAAECggEAKyLEhFedmPOBdRPzczALmnMQyYFwtq1VE3kjugwwPDmF4dhdJ4XWDk+VjXfkUPDxJRrOJf8NLUvTfdmpnxcfZw5pCjZSFoUcSTKPaJF92Yaio/p3CaXXLrjPiDufFAhRscO8trfMRjd+ywgRiIcmMQJFj5ErBqUcV6Cm7S+iFERddb1a8u3i3ZmiL8idrW2O33O4Ze5F1I9IQk9YJTMf378ZFxCsWeC7ASWb2DTBIOx5FbYnJnXTtSbQ03UBlF1a8b/EcPkHes6ZAesb71BVmtVUf5u8YGbWuoCxSEjj0RDRZUzRMCXJYjKFhn523Pj4gDocYiuGgou6BQ54vAiq3QKBgQDnrYvWu9OLUiHUti+q884q89XWM90Nl3C8Lf1YcdTKNfVtz8tgUzNs0TvDyFdl9exMQ0JaZ0cjtqDkXwVI5nuUV8rlZz0a9DNMiku+1qJG7wa1HNAVXOTR8Jy1qJM2yKboDJ5IkzOk6qs0Q5XjtOZ3djYu1Vr4H46/dOyv6KGAXQKBgQDyNtTZnKYqgQ7qJdMpzRG2ieNsV3p7sFLM6Kr0WUK1OaO4ml4vwEPRPtjEm4yxnfKIGOsJfRzK3fR968xO3trbDoE8sc1BD3KPk3N+FcHBsdz01AjsbefuEYyCj0WdZ+p2C8jXWCrdzXWTDyMUu7fJCSK692Y3U4UwOoih1EZdLQKBgDt/r30raa3LHxMg4ucVjCotywoi8a9u6C375aeUPGDeMLTN+942H+vfZGjoh8GQwenoEB9ljLiYS4O6/4F0Z+J2FY6Kz/+fHZmXntJZDv4GnwNebfY+kCl2kNh6faxBJdRMgU4EN+wvtF5MyS7co5+khU/LzXwRprh2z95xcSClAoGAMrjAwgFH2sbD9MyO98X125/sfBelkplN/ZMLmbfHfGPhoRrSdnVwvjTG+AUK5XcJ/u4Y6hfFG8Z3GJd0ZV/NvOnArpEdRm6RsvdRzCmKNaGlf6+XXst1MimCnySZk2jbdHTrTg0sjN4lrPISipj016iOGVENmrBmx766NglZ4tECgYAlXEUGEHo6Ctl23IaIVunXpu364K/ULUW3KlUXaSDoN2gvS5YOyK2UleQwuHNWun0Q/pC9P7nLdM8qj1qqwDmg1LhiSgIG6dBfWwKc/u0I6lSZb1gOvxqfS7CP0YnSIn3XDGYxa/RfWMtezlqIB5O8qNXiYa4rIOPPPuHmWW4fjA==";

        try {
            PRIVATE_KEY = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(cc.astralis.protection.util.Base64.decode(privateKey)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NativeObfuscate
    public static String encrypt(String data, byte[] key, byte[] aad) {
        try {
            byte[] ct = AeadChaCha20Poly1305.encrypt(
                    data.getBytes(StandardCharsets.UTF_8), key, aad
            );
            return Base64.getEncoder().encodeToString(ct);
        } catch (Exception e) { throw new RuntimeException("Encryption failed", e); }
    }

    @NativeObfuscate
    public static String decrypt(String base64, byte[] key, byte[] aad) {
        try {
            byte[] ct = cc.astralis.protection.util.Base64.decode(base64);
            byte[] pt = AeadChaCha20Poly1305.decrypt(ct, key, aad);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Encryption failed " + e.getMessage(), e); }
    }

    public static String encrypt(String data, byte[] key, long aad) {
        return encrypt(data, key, longToBytes(aad));
    }
    public static String decrypt(String data, byte[] key, long aad) {
        return decrypt(data, key, longToBytes(aad));
    }

    private static byte[] longToBytes(long x) {
        return ByteBuffer.allocate(Long.BYTES).putLong(x).array();
    }
}