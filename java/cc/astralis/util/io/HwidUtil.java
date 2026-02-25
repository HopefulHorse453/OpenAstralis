package cc.astralis.util.io;

import cc.astralis.Astralis;
import org.lwjgl.opengl.GL11;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ThreadLocalRandom;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HwidUtil {
    public static String getHardwareID() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();

            sb.append(PlatformInfo.getOSName())
                    .append(PlatformInfo.getArch())
                    .append(PlatformInfo.getVersion())
                    .append(PlatformInfo.getCores())
                    .append(PlatformInfo.getEnv("PROCESSOR_IDENTIFIER"))
                    .append(PlatformInfo.getEnv("PROCESSOR_ARCHITECTURE"))
                    .append(PlatformInfo.getEnv("PROCESSOR_ARCHITEW6432"))
                    .append(PlatformInfo.getEnv("NUMBER_OF_PROCESSORS"))
                    .append(PlatformInfo.getEnv("COMPUTERNAME"))
                    .append(PlatformInfo.getEnv("os"))
                    .append(PlatformInfo.getLang())
                    .append(PlatformInfo.getEnv("SystemRoot"))
                    .append(PlatformInfo.getEnv("HOMEDRIVE"))
                    .append(PlatformInfo.getEnv("PROCESSOR_LEVEL"))
                    .append(PlatformInfo.getEnv("PROCESSOR_REVISION"))
                    .append(PlatformInfo.getEnv("HOME"))
                    .append(PlatformInfo.getEnv("HOSTNAME"))
                    .append(PlatformInfo.getEnv("SHELL"))
                    .append(PlatformInfo.getEnv("LOGNAME"))
                    .append(PlatformInfo.getEnv("USERNAME"));

            byte[] raw = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(raw);
        } catch (NoSuchAlgorithmException e) {
            // crash
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
                    for (long l = Long.MIN_VALUE; l < Long.MAX_VALUE; ++l) {
                        --l;
                    }
                }
            return "################";
        }
    }

    private static String bytesToHex(byte[] input) {
        StringBuilder hex = new StringBuilder();
        for (byte b : input) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    public static class PlatformInfo {
        private static final OS CURRENT_OS = detect();

        public static String getOSName() {
            return CURRENT_OS.getPrettyName();
        }

        public static String getArch() {
            return System.getProperty("os.arch", "unknown");
        }

        public static String getVersion() {
            return System.getProperty("os.version", "unknown");
        }

        public static int getCores() {
            return Runtime.getRuntime().availableProcessors();
        }

        public static String getLang() {
            return System.getProperty("user.language", "en");
        }

        public static String getEnv(String key) {
            String val = System.getenv(key);
            return val != null ? val : "";
        }

        public static OS detect() {
            String raw = System.getProperty("os.name", "").toLowerCase();
            if (raw.contains("win")) return OS.WIN;
            if (raw.contains("mac")) return OS.MAC;
            if (raw.contains("nux") || raw.contains("nix") || raw.contains("aix")) return OS.LNX;
            return OS.UNK;
        }

        public enum OS {
            WIN("Windows"), MAC("MacOS"), LNX("Linux"), UNK("Unknown");
            private final String name;
            OS(String name) { this.name = name; }
            String getPrettyName() { return name; }
        }
    }
}
