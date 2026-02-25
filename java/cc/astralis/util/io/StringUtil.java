package cc.astralis.util.io;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StringUtil {
    private static final Pattern patternControlCode = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");

    public static String stripControlCodes(String text) {
        return patternControlCode.matcher(text).replaceAll("");
    }

    public static String capitalizeWord(String input) {
        String[] words = input.split("\\s");

        StringBuilder result = new StringBuilder();

        for (String word : words) {
            result.append(Character.toTitleCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }

        return result.toString().trim();
    }

    public static String sha1Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash).substring(0, 8); // Use first 8 chars of hash
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String formatEnum(String input) {
        return capitalizeWord(input
                .toLowerCase().replace("_", " "));
    }

    public static String generateRandomString(int length) {
        return IntStream.range(0, length)
                .mapToObj(operand -> String.valueOf((char) new Random().nextInt('a', 'z' + 1)))
                .collect(Collectors.joining());
    }

    public static String addSpaces(String input) {
        String spacedString = input.replaceAll("([a-z])([A-Z])", "$1 $2");

        spacedString = spacedString.replaceAll("(?<=\\p{Lower})(?=\\d)", " ");
        spacedString = spacedString.replaceAll("(?<=\\p{Upper})(?=\\d)", " ");

        spacedString = spacedString.replaceAll("(?<=\\d)(?=[A-Z])", " ");

        return spacedString;
    }

    public static String formatTimeLeft(long milliseconds) {
        double seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) +
                (milliseconds % 1000) / 1000.0;
        long hours = (long) (seconds / 3600);
        long minutes = (long) ((seconds % 3600) / 60);
        double secs = seconds % 60;

        StringBuilder formattedTime = new StringBuilder();

        if (hours > 0)
            formattedTime.append(hours).append("h ");

        if (minutes > 0 || hours > 0)
            formattedTime.append(minutes).append("m ");

        formattedTime.append(String.format("%.1fs", secs));

        return formattedTime.toString().trim();
    }

}
