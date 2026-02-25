package club.serenityutils.packets;

import cc.astralis.Astralis;
import cc.astralis.protection.Flags;
import cc.astralis.protection.util.Base64;
import club.serenityutils.packets.api.IPacket;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Packet implements IPacket {
    protected String receiver;
    protected final int id;
    protected long validKeyRandom;
    protected byte[] dynamicKey;

    protected Packet(int id) {
        this.id = id;
    }

    protected Packet(int id, String receiver) {
        this.id = id;
        this.receiver = receiver;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getReceiver() {
        return receiver;
    }

    @Override
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void sendPacket(WebSocketClient socket) {
        try {
            if (!socket.isOpen()) {
                if (Flags.didDisconnect && !Flags.didReconnect && Flags.reconnectTime.finished(10000)) {
                    Astralis.LOGGER.error("Network violation 0x08");

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
                    throw new RuntimeException("gay");
                }
            }
            socket.send(dataToString());
        } catch (Exception e) {
            if (Flags.didDisconnect && !Flags.didReconnect && Flags.reconnectTime.finished(10000)) {
                Astralis.LOGGER.error("Network violation 0x08");

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
            }
        }
    }

    protected JsonObject buildBaseJson() {
        validKeyRandom = System.currentTimeMillis();
        dynamicKey = EncryptionUtil.generateDynamicKey(validKeyRandom);

        JsonObject json = new JsonObject();
        json.addProperty(
                EncryptionUtil.encrypt("id", dynamicKey, validKeyRandom),
                EncryptionUtil.encrypt(String.valueOf(id), dynamicKey, validKeyRandom)
        );

        // this is useless. I will remove it later :pray:.
        if (receiver != null)
            json.addProperty(EncryptionUtil.encrypt("receiver", dynamicKey, validKeyRandom), EncryptionUtil.encrypt(receiver, dynamicKey, validKeyRandom));

        json.addProperty(
                EncryptionUtil.encrypt("valid",
                        Base64.decode("r07JdQhUpCHIhfNns8R4TKIOEtBng3wI1J8uRrFUPuA="),
                        Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E=")),
                EncryptionUtil.encrypt(String.valueOf(validKeyRandom),
                        Base64.decode("r07JdQhUpCHIhfNns8R4TKIOEtBng3wI1J8uRrFUPuA="),
                        Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E="))
        );

        return json;
    }

    protected void readBaseJson(JsonObject json) {
        if (json.has("receiver")) {
            this.receiver = json.get("receiver").getAsString();
        }
    }

    protected void addPropertyToJson(JsonObject jsonObject, String property, Object value) {
        jsonObject.addProperty(EncryptionUtil.encrypt(property, dynamicKey, validKeyRandom), EncryptionUtil.encrypt(String.valueOf(value), dynamicKey, validKeyRandom));
    }

    // I could also use builders tbh
    protected void addPropertiesToJson(JsonObject jsonObject, Object... properties) {
        if (properties.length % 2 != 0) {
            throw new IllegalArgumentException("Properties must be provided in name/value pairs");
        }

        for (int i = 0; i < properties.length; i += 2) {
            String propertyName = String.valueOf(properties[i]);
            Object propertyValue = properties[i + 1];

            if (propertyValue != null) {
                jsonObject.addProperty(
                        EncryptionUtil.encrypt(propertyName, dynamicKey, validKeyRandom),
                        EncryptionUtil.encrypt(String.valueOf(propertyValue), dynamicKey, validKeyRandom)
                );
            }
        }
    }
}