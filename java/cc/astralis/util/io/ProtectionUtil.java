package cc.astralis.util.io;

import cc.astralis.Astralis;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class ProtectionUtil {
    // this method should only be called in classes which are not obfuscated.
    public static void crash() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);

            long corruptValue = ThreadLocalRandom.current().nextLong();
            long randomAddress = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
            int haltCode = ThreadLocalRandom.current().nextInt(1, 256);

            unsafe.putLong(Thread.currentThread(), 8L, corruptValue);
            unsafe.putAddress(randomAddress, 0);
            Runtime.getRuntime().halt(haltCode);

        } catch (Throwable ignored) {
            crash();

            for (long l = Long.MIN_VALUE; l < Long.MAX_VALUE; ++l) {
                --l;
            }
        }

        for (long l = Long.MIN_VALUE; l < Long.MAX_VALUE; ++l) {
            --l;
        }
    }
}
