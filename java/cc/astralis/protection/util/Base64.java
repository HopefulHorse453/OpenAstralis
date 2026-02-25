package cc.astralis.protection.util;

// ty Liticane :pray:.
public class Base64 {
    public static String encode(final byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }

        final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        final int outputLen = ((data.length + 2) / 3) * 4;
        final char[] ret = new char[outputLen];

        int iIndex = 0;
        int oIndex = 0;

        final int groups = data.length / 3;
        for (int i = 0; i < groups; i++) {
            int o = ((data[iIndex++] & 0xFF) << 16) | ((data[iIndex++] & 0xFF) << 8) | (data[iIndex++] & 0xFF);

            ret[oIndex++] = alphabet[(o >>> 18) & 63];
            ret[oIndex++] = alphabet[(o >>> 12) & 63];
            ret[oIndex++] = alphabet[(o >>> 6) & 63];
            ret[oIndex++] = alphabet[o & 63];
        }

        final int remainder = data.length % 3;
        if (remainder == 1) {
            final int o = (data[iIndex] & 0xFF) << 16;

            ret[oIndex++] = alphabet[(o >>> 18) & 63];
            ret[oIndex++] = alphabet[(o >>> 12) & 63];
            ret[oIndex++] = '=';
            ret[oIndex] = '=';
        } else if (remainder == 2) {
            final int o = ((data[iIndex++] & 0xFF) << 16) | ((data[iIndex] & 0xFF) << 8);

            ret[oIndex++] = alphabet[(o >>> 18) & 63];
            ret[oIndex++] = alphabet[(o >>> 12) & 63];
            ret[oIndex++] = alphabet[(o >>> 6) & 63];
            ret[oIndex] = '=';
        }

        return new String(ret);
    }

    public static byte[] decode(final String base64) {
        if (base64 == null || base64.isEmpty()) {
            return new byte[0];
        }

        int nonWsLen = 0;
        for (int i = 0; i < base64.length(); i++) {
            final char o = base64.charAt(i);
            if (!Character.isWhitespace(o)) {
                nonWsLen++;
            }
        }

        if (nonWsLen % 4 != 0) {
            throw new IllegalArgumentException("Invalid Base64!");
        }

        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        final int[] inverseAlphabet = new int[256];
        for (int i = 0; i < alphabet.length(); i++) {
            inverseAlphabet[alphabet.charAt(i)] = i;
        }
        inverseAlphabet['='] = 0;

        int pad = 0;
        int end = base64.length() - 1;

        while (end >= 0 && Character.isWhitespace(base64.charAt(end))) {
            end--;
        }

        if (end >= 0 && base64.charAt(end) == '=') {
            pad++;
            end--;

            if (end >= 0 && base64.charAt(end) == '=') {
                pad++;
            }
        }

        final int byteLen = (nonWsLen * 6 / 8) - pad;
        final byte[] out = new byte[byteLen];

        final int[] quartet = new int[4];

        int outIndex = 0;
        int q = 0;

        for (int i = 0; i < base64.length(); i++) {
            final char a = base64.charAt(i);
            if (Character.isWhitespace(a)) {
                continue;
            }

            final int inv;
            if (a < 256) {
                inv = inverseAlphabet[a];
            } else {
                throw new IllegalArgumentException("Invalid Base64 character");
            }

            quartet[q++] = inv;
            if (q == 4) {
                final int b = (quartet[0] << 18) | (quartet[1] << 12) | (quartet[2] << 6) | quartet[3];

                if (outIndex < byteLen)
                    out[outIndex++] = (byte) ((b >>> 16) & 0xFF);
                if (outIndex < byteLen)
                    out[outIndex++] = (byte) ((b >>> 8) & 0xFF);
                if (outIndex < byteLen)
                    out[outIndex++] = (byte) (b & 0xFF);

                q = 0;
            }
        }

        return out;
    }
}