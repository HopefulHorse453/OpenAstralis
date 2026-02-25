package club.serenityutils.utils;


import io.github.kawase.NativeObfuscate;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * @author Reowya (ily)
 * @since nigger
 */
public class AeadChaCha20Poly1305 {
    private static final int BLOCK_SIZE = 64;
    private static final int NONCE_SIZE = 12;
    private static final int TAG_SIZE = 16;
    private static final int KEY_SIZE = 32;

    public static class InvalidTagException extends Exception {
        public InvalidTagException(String message) {
            super(message);
        }
    }

    @NativeObfuscate
    public static byte[] encrypt(byte[] data, byte[] key, byte[] aad) {
        if (key.length != KEY_SIZE) {
            throw new IllegalArgumentException("Key size must be 32 bytes");
        }

        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[NONCE_SIZE];
        random.nextBytes(nonce);

        ChaCha20Poly1305 c20p1305 = new ChaCha20Poly1305(key, nonce, aad);

        byte[] ciphertext = c20p1305.encrypt(data);
        byte[] tag = c20p1305.finish();

        ByteBuffer buffer = ByteBuffer.allocate(nonce.length + ciphertext.length + tag.length);
        buffer.put(nonce);
        buffer.put(ciphertext);
        buffer.put(tag);
        return buffer.array();
    }

    @NativeObfuscate
    public static byte[] decrypt(byte[] data, byte[] key, byte[] aad) throws Exception {
        if (key.length != KEY_SIZE) {
            throw new IllegalArgumentException("Key size must be 32 bytes");
        }

        if (data.length < NONCE_SIZE + TAG_SIZE) {
            throw new IllegalArgumentException("Data too short");
        }

        byte[] nonce = Arrays.copyOfRange(data, 0, NONCE_SIZE);
        byte[] ciphertext = Arrays.copyOfRange(data, NONCE_SIZE, data.length - TAG_SIZE);
        byte[] tag = Arrays.copyOfRange(data, data.length - TAG_SIZE, data.length);

        ChaCha20Poly1305 c20p1305 = new ChaCha20Poly1305(key, nonce, aad);
        byte[] plaintext = c20p1305.decrypt(ciphertext);
        byte[] computedTag = c20p1305.finish();

        if (!Arrays.equals(tag, computedTag)) {
            throw new InvalidTagException("Failed to authenticate message, either tampered or corrupted!");
        }

        return plaintext;
    }

    private static class ChaCha20 {
        private static final int[] CONSTANTS = {0x61707865, 0x3320646e, 0x79622d32, 0x6b206574};
        private final byte[] key;
        private final byte[] nonce;
        private int count;

        public ChaCha20(byte[] key, byte[] nonce, int count) {
            if (key.length != KEY_SIZE) {
                throw new IllegalArgumentException("Key must be 32 bytes");
            }
            if (nonce.length != NONCE_SIZE) {
                throw new IllegalArgumentException("Nonce must be 12 bytes");
            }
            if (count < 0) {
                throw new IllegalArgumentException("Count must be non-negative");
            }
            this.key = key;
            this.nonce = nonce;
            this.count = count;
        }

        private static int rotateLeft(int value, int count) {
            return (value << count) | (value >>> (32 - count));
        }

        private static void quarterRound(int[] state, int a, int b, int c, int d) {
            state[a] = (state[a] + state[b]);
            state[d] = rotateLeft(state[d] ^ state[a], 16);
            state[c] = (state[c] + state[d]);
            state[b] = rotateLeft(state[b] ^ state[c], 12);
            state[a] = (state[a] + state[b]);
            state[d] = rotateLeft(state[d] ^ state[a], 8);
            state[c] = (state[c] + state[d]);
            state[b] = rotateLeft(state[b] ^ state[c], 7);
        }

        private static void doubleRound(int[] state) {
            quarterRound(state, 0, 4, 8, 12);
            quarterRound(state, 1, 5, 9, 13);
            quarterRound(state, 2, 6, 10, 14);
            quarterRound(state, 3, 7, 11, 15);
            quarterRound(state, 0, 5, 10, 15);
            quarterRound(state, 1, 6, 11, 12);
            quarterRound(state, 2, 7, 8, 13);
            quarterRound(state, 3, 4, 9, 14);
        }

        private int[] getState() {
            int[] state = new int[16];
            System.arraycopy(CONSTANTS, 0, state, 0, 4);
            ByteBuffer keyBuffer = ByteBuffer.wrap(key).order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 4; i < 12; i++) {
                state[i] = keyBuffer.getInt();
            }
            state[12] = count;
            ByteBuffer nonceBuffer = ByteBuffer.wrap(nonce).order(ByteOrder.LITTLE_ENDIAN);
            state[13] = nonceBuffer.getInt();
            state[14] = nonceBuffer.getInt(4);
            state[15] = nonceBuffer.getInt(8);
            return state;
        }

        private byte[] block(byte[] data) {
            int[] state = getState();
            int[] workingState = state.clone();

            for (int i = 0; i < 10; i++) {
                doubleRound(workingState);
            }

            ByteBuffer keyStream = ByteBuffer.allocate(BLOCK_SIZE).order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < 16; i++) {
                keyStream.putInt((state[i] + workingState[i]));
            }

            byte[] result = new byte[data.length];
            byte[] stream = keyStream.array();
            for (int i = 0; i < data.length; i++) {
                result[i] = (byte) (data[i] ^ stream[i]);
            }
            count++;
            return result;
        }

        public byte[] encrypt(byte[] data) {
            byte[] result = new byte[data.length];
            int offset = 0;
            for (int i = 0; i < data.length; i += BLOCK_SIZE) {
                byte[] chunk = Arrays.copyOfRange(data, i, Math.min(i + BLOCK_SIZE, data.length));
                byte[] encryptedChunk = block(chunk);
                System.arraycopy(encryptedChunk, 0, result, offset, encryptedChunk.length);
                offset += encryptedChunk.length;
            }
            return result;
        }

        public byte[] decrypt(byte[] data) {
            return encrypt(data);
        }
    }

    public static class Poly1305 {
        private static final BigInteger P = BigInteger.valueOf(2).pow(130).subtract(BigInteger.valueOf(5));
        private static final BigInteger MASK_R = new BigInteger("0ffffffc0ffffffc0ffffffc0fffffff", 16);
        private static final BigInteger MASK_TAG = new BigInteger("ffffffffffffffffffffffffffffffff", 16);

        private BigInteger accumulator;
        private final BigInteger r;
        private final BigInteger s;

        public Poly1305(byte[] key) {
            if (key.length != 32) {
                throw new IllegalArgumentException("Key must be 32 bytes");
            }

            ByteBuffer rBuffer = ByteBuffer.wrap(Arrays.copyOfRange(key, 0, 16)).order(ByteOrder.LITTLE_ENDIAN);
            r = new BigInteger(1, rBuffer.array()).and(MASK_R);
            ByteBuffer sBuffer = ByteBuffer.wrap(Arrays.copyOfRange(key, 16, 32)).order(ByteOrder.LITTLE_ENDIAN);
            s = new BigInteger(1, sBuffer.array());
            accumulator = BigInteger.ZERO;
        }

        public void update(byte[] data) {
            for (int i = 0; i < data.length; i += 16) {
                byte[] chunk = Arrays.copyOfRange(data, i, Math.min(i + 16, data.length));
                byte[] paddedChunk = new byte[chunk.length + 1];
                System.arraycopy(chunk, 0, paddedChunk, 0, chunk.length);
                paddedChunk[paddedChunk.length - 1] = 1;
                BigInteger value = new BigInteger(1, paddedChunk);
                accumulator = accumulator.add(value).mod(P);
                accumulator = accumulator.multiply(r).mod(P);
            }
        }

        public byte[] finish() {
            BigInteger result = accumulator.add(s).and(MASK_TAG);
            byte[] resultBytes = result.toByteArray();
            byte[] tag = new byte[16];
            int offset = Math.max(0, resultBytes.length - 16);
            System.arraycopy(resultBytes, offset, tag, 16 - (resultBytes.length - offset), resultBytes.length - offset);
            return tag;
        }
    }

    private static class ChaCha20Poly1305 {
        private final byte[] addition;
        private final ChaCha20 c20;
        private final Poly1305 p1305;
        private long dataLength;

        public ChaCha20Poly1305(byte[] key, byte[] nonce) {
            this(key, nonce, "ChaCha20Poly1305".getBytes());
        }

        public ChaCha20Poly1305(byte[] key, byte[] nonce, byte[] addition) {
            if (key.length != KEY_SIZE) {
                throw new IllegalArgumentException("Key must be 32 bytes");
            }

            if (nonce.length != NONCE_SIZE) {
                throw new IllegalArgumentException("Nonce must be 12 bytes");
            }

            this.addition = addition;
            this.dataLength = 0;

            byte[] polyKey = new ChaCha20(key, nonce, 0).encrypt(new byte[BLOCK_SIZE]);
            this.p1305 = new Poly1305(Arrays.copyOf(polyKey, KEY_SIZE));
            this.c20 = new ChaCha20(key, nonce, 1);

            byte[] paddedAdditionalData = pad(addition.length, 16);
            byte[] adWithPadding = new byte[addition.length + paddedAdditionalData.length];
            System.arraycopy(addition, 0, adWithPadding, 0, addition.length);
            System.arraycopy(paddedAdditionalData, 0, adWithPadding, addition.length, paddedAdditionalData.length);
            p1305.update(adWithPadding);
        }

        @SuppressWarnings("SameParameterValue")
        private static byte[] pad(int length, int blockSize) {
            int paddingLength = (blockSize - (length % blockSize)) % blockSize;
            return new byte[paddingLength];
        }

        public byte[] encrypt(byte[] data) {
            byte[] ciphertext = c20.encrypt(data);
            byte[] paddedCiphertext = pad(ciphertext.length, 16);
            byte[] dataWithPadding = new byte[ciphertext.length + paddedCiphertext.length];
            System.arraycopy(ciphertext, 0, dataWithPadding, 0, ciphertext.length);
            System.arraycopy(paddedCiphertext, 0, dataWithPadding, ciphertext.length, paddedCiphertext.length);
            p1305.update(dataWithPadding);
            dataLength += ciphertext.length;
            return ciphertext;
        }

        public byte[] decrypt(byte[] data) {
            byte[] paddedData = pad(data.length, 16);
            byte[] dataWithPadding = new byte[data.length + paddedData.length];
            System.arraycopy(data, 0, dataWithPadding, 0, data.length);
            System.arraycopy(paddedData, 0, dataWithPadding, data.length, paddedData.length);
            p1305.update(dataWithPadding);
            dataLength += data.length;
            return c20.decrypt(data);
        }

        public byte[] finish() {
            ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(addition.length);
            buffer.putLong(dataLength);
            p1305.update(buffer.array());
            return p1305.finish();
        }
    }
}