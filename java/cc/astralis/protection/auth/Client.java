package cc.astralis.protection.auth;

import cc.astralis.Astralis;
import cc.astralis.event.events.impl.network.BackendMessageEvent;
import cc.astralis.module.Module;
import cc.astralis.protection.Flags;
import cc.astralis.protection.util.Base64;
import cc.astralis.util.io.HwidUtil;
import club.serenityutils.modules.ModuleMetaData;
import club.serenityutils.packets.api.IPacket;
import club.serenityutils.packets.impl.*;
import club.serenityutils.packets.impl.cloud.FetchCloudConfigsPacket;
import club.serenityutils.packets.impl.cloud.FetchCloudConfigsResponsePacket;
import cc.astralis.util.math.TimeUtil;
import club.serenityutils.packets.impl.module.FetchModuleInfoPacket;
import club.serenityutils.packets.impl.module.FetchModuleInfoResponsePacket;
import club.serenityutils.utils.EncryptionUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.kawase.NativeObfuscate;
import io.github.kawase.VMProtect;
import lombok.Getter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Client extends WebSocketClient {
    public final TimeUtil keepAliveDelay, lastReceivedKeepAliveTime;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    private boolean failedToReconnect;
    private boolean didHandShake;
    private int reconnectAttempts = 0;

    @Getter
    private JsonObject jsonData;

    public Client(String client) throws Exception {
        super(new URI(client));

        if (!client.equals("wss://ws.serenityutils.club")) {
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

        this.keepAliveDelay = new TimeUtil();
        this.lastReceivedKeepAliveTime = new TimeUtil();
    }

    @NativeObfuscate
    @VMProtect(VMProtect.ProtectionLevel.STANDARD)
    public void start() {
        try {
            this.connectBlocking();
            new HandShakePacket(
                    Astralis.getInstance().getClientProfileMeta().getType().toString(),
                    HwidUtil.getHardwareID(),
                    Astralis.getInstance().getClientProfileMeta().getHash(),
                    Astralis.getInstance().getClientProfileMeta().getVersion()
            ).sendPacket(this);

            new Thread(() -> {
                while (true) {
                    firstThread();
                }
            }, "Terrorist organization #1").start();

            new Thread(() -> {
                while (true) {
                    secondThread();
                }
            }, "Terrorist organization #2").start();

        } catch (Exception e) {
            Astralis.LOGGER.error("Network violation 0x02");

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

    private void firstThread() {
        Flags.firstThreadRunning = true;

        try {
            if (!this.isOpen() && (failedToReconnect || (Flags.didDisconnect && !Flags.didReconnect && Flags.reconnectTime.finished(10000)))) {
                Astralis.LOGGER.error("Network violation 0x03");

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
        } catch (Exception e) {
            Astralis.LOGGER.error("Network violation 0x04");

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

        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {
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

            } catch (Throwable ignored2) {
                for (long l = Long.MIN_VALUE; l < Long.MAX_VALUE; ++l) {
                    --l;
                }
            }
        }
    }

    private void secondThread() {
        Flags.secondThreadRunning = true;

        if (keepAliveDelay.finished(500) && !Flags.sessionToken.equalsIgnoreCase("none")) {
            new ClientKeepAlivePacket(Flags.sessionToken).sendPacket(this);
            keepAliveDelay.reset();
        }

        if (lastReceivedKeepAliveTime.finished(60000)) {
            Flags.keepAliveWorking = false;
            Astralis.LOGGER.error("Network violation 0x05");

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

        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {
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

            } catch (Throwable ignored2) {
                for (long l = Long.MIN_VALUE; l < Long.MAX_VALUE; ++l) {
                    --l;
                }
            }
        }
    }

    @NativeObfuscate
    @VMProtect(VMProtect.ProtectionLevel.ULTRA)
    @Override
    public void onMessage(String encryptedJsonData) {
        try {
            byte[] baseKey = Base64.decode("r07JdQhUpCHIhfNns8R4TKIOEtBng3wI1J8uRrFUPuA=");
            byte[] secondKey = Base64.decode("6QfqCRgmWk9qCVa3+n30xuYh9aH/TJurNC5G81LPYoE=");
            // pretty self-explanatory, there is prob a better way of doing this.
            byte[] staticWatermark = Base64.decode("QVNVSERIVUpJS1NaaElVRERBSU9TSERJT0FTREhBU0RIQVNIREFTQURTSFVEQVNIRDc5VThJR1dRRUc3OFFURVdHQUlER0E=");

            String outerDecrypted = EncryptionUtil.decrypt(encryptedJsonData, secondKey, staticWatermark);

            JsonObject encryptedLayer = JsonParser.parseString(outerDecrypted).getAsJsonObject();
            String validKeyEncrypted = null;
            String valuePropertyName = null;

            for (Map.Entry<String, JsonElement> entry : encryptedLayer.entrySet()) {
                try {
                    String potentialKey = EncryptionUtil.decrypt(entry.getKey(), baseKey, staticWatermark);
                    if ("valid".equals(potentialKey)) {
                        validKeyEncrypted = entry.getValue().getAsString();
                        valuePropertyName = entry.getKey();
                        break;
                    }
                } catch (Exception ignored) {}
            }

            if (validKeyEncrypted == null)
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

            long validKey = Long.parseLong(EncryptionUtil.decrypt(validKeyEncrypted, baseKey, staticWatermark));
            byte[] dynamicKey = EncryptionUtil.generateDynamicKey(validKey);

            JsonObject decryptedJson = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : encryptedLayer.entrySet()) {
                try {
                    if (Objects.equals(entry.getKey(), valuePropertyName)) {
                        decryptedJson.addProperty("valid", validKey);
                        continue;
                    }

                    String decryptedKey = EncryptionUtil.decrypt(entry.getKey(), dynamicKey, validKey);
                    JsonElement value = entry.getValue();

                    if (value.isJsonArray()) {
                        JsonArray encryptedArray = value.getAsJsonArray();
                        JsonArray decryptedArray = new JsonArray();

                        for (JsonElement element : encryptedArray) {
                            JsonObject encryptedObj = element.getAsJsonObject();
                            JsonObject decryptedObj = new JsonObject();

                            for (Map.Entry<String, JsonElement> objEntry : encryptedObj.entrySet()) {
                                String objKey = EncryptionUtil.decrypt(objEntry.getKey(), dynamicKey, validKey);
                                String objValue = EncryptionUtil.decrypt(objEntry.getValue().getAsString(), dynamicKey, validKey);
                                decryptedObj.addProperty(objKey, objValue);
                            }

                            decryptedArray.add(decryptedObj);
                        }

                        decryptedJson.add(decryptedKey, decryptedArray);
                    } else {
                        String decryptedValue = EncryptionUtil.decrypt(value.getAsString(), dynamicKey, validKey);
                        decryptedJson.addProperty(decryptedKey, decryptedValue);
                    }
                } catch (Exception e) {
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

            int packetId = Integer.parseInt(decryptedJson.get("id").getAsString());
            IPacket packet = Astralis.getInstance().getPacketManager().createPacket(packetId);
            packet.fromJson(decryptedJson);

            switch (packetId) {
                case 1 -> {
                    MessagePacket messagePacket = (MessagePacket) packet;
                    Astralis.getInstance().getEventManager().call(new BackendMessageEvent(messagePacket.getMessage()));
                }
                case 8 -> {
                    FetchModuleInfoResponsePacket fetchModuleInfoResponsePacket = (FetchModuleInfoResponsePacket) packet;

                    for (ModuleMetaData moduleMetaData : fetchModuleInfoResponsePacket.getModuleMetaData()) {
                        for (Module module : Astralis.getInstance().getModuleManager().getModules()) {
                            if (!module.getModuleMetaData().getName().equals("none") ||
                                    !module.getModuleMetaData().getDescription().equals("none")) {
                                continue;
                            }

                            module.getModuleMetaData().setName(moduleMetaData.getName());
                            module.getModuleMetaData().setDescription(moduleMetaData.getDescription());
                            break;
                        }
                    }

                    Flags.gotModuleInfo = true;
                }

                case 10 -> {
                    FetchCloudConfigsResponsePacket fetchCloudConfigsResponsePacket = (FetchCloudConfigsResponsePacket) packet;
                    Flags.cloudConfigs = fetchCloudConfigsResponsePacket.getCloudConfigList();
                }

                case 5 -> {
                    DataTransferPacket dataTransferPacket = (DataTransferPacket) packet;
                    String msg = dataTransferPacket.getMessage();
                    String header = dataTransferPacket.getHeader();

                    switch (header) {
                        case "token" -> {
                            if (!didHandShake) {
                                Flags.sessionToken = msg;
                                didHandShake = true;
                            }
                        }

                        case "userInfo" -> {
                            if (!Flags.isNotAuthenticated) {
                                Flags.user.setName(msg);

                                /*System.out.println("---- FLAGS DEBUG ----");
                                System.out.println("isNotAuthenticated: " + Flags.isNotAuthenticated);
                                System.out.println("authStatus: " + Flags.authStatus);
                                System.out.println("authGuiShown: " + Flags.authGuiShown);
                                System.out.println("authPacketSent: " + Flags.authPacketSent);
                                System.out.println("sessionToken: " + Flags.sessionToken);
                                System.out.println("keepAliveWorking: " + Flags.keepAliveWorking);
                                System.out.println("didDisconnect: " + Flags.didDisconnect);
                                System.out.println("didReconnect: " + Flags.didReconnect);
                                System.out.println("uid: " + Flags.uid);
                                System.out.println("reconnectTime elapsed: " + Flags.reconnectTime.getElapsedTime());
                                System.out.println("---------------------");*/
                                // its a bit retarded to move this here but oh well.
                                Astralis.getInstance().getComponentManager().registerComponents();
                                Astralis.getInstance().getModuleManager().registerModules();
                                Astralis.getInstance().getCommandManager().registerCommands();
                                Astralis.getInstance().getAnimationManager().start();
                                Astralis.getInstance().getAltConfig().loadConfig();
                                Astralis.getInstance().getDraggableConfig().loadConfig();
                                Astralis.getInstance().getConfigManager().init();

                                new FetchModuleInfoPacket().sendPacket(this);
                                new FetchCloudConfigsPacket().sendPacket(this);
                                Flags.didSendFetchModuleInfoPacket = true;
                            }
                        }

                        case "auth" -> {
                            if (msg.equalsIgnoreCase("gud boy") && Flags.authGuiShown) {
                                Flags.isNotAuthenticated = false;
                                Flags.authStatus = msg;
                                Flags.user.setUid(Astralis.getInstance().getAuthScreen().lastTriedUid);
                                /*Flags.uid = Astralis.getInstance().getAuthScreen().lastTriedUid;*/
                            }
                        }

                        case "bad" -> {
                            if (msg.equalsIgnoreCase("crash")) {
                                Astralis.LOGGER.error("Outdated client version.");

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
                                failedToReconnect = true;
                                Flags.reconnectTime.setLastMS(10000);
                            }
                        }
                    }
                }

                case 7 -> {
                    lastReceivedKeepAliveTime.reset();
                    jsonData = decryptedJson;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Astralis.LOGGER.error("Network violation 0x01");

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

    @Override
    public void onOpen(ServerHandshake handshake) {
        //Astralis.LOGGER.info("Connected");
        Flags.didReconnect = true;
        Flags.didDisconnect = false;
        Flags.reconnectTime.reset();
        failedToReconnect = false;

        Flags.keepAliveWorking = true;
        lastReceivedKeepAliveTime.reset();
    }

    @Override
    public void onError(Exception ex) {
        if (ex instanceof java.net.SocketException &&
                ex.getMessage().toLowerCase().contains("permission denied")) {
            return;
        }

        if (!Flags.keepAliveWorking ||
                (Flags.didDisconnect && !Flags.didReconnect && Flags.reconnectTime.finished(10000))) {
            Astralis.LOGGER.error("Network violation 0x11");

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

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Flags.didDisconnect = true;
        Flags.didReconnect = false;
        Flags.reconnectTime.reset();
        //Astralis.LOGGER.warn("Disconnected");
        attemptReconnect();
    }

    @NativeObfuscate
    public void attemptReconnect() {
        // 10 is enough ;3
        if (reconnectAttempts >= 10) {
            failedToReconnect = true;
            Astralis.LOGGER.error("Network violation 0x10");

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
            return;
        }

        reconnectAttempts++;

        executorService.schedule(() -> {
            try {
                // cleanup
                if (this.isOpen()) {
                    this.close();
                }

                boolean success = this.reconnectBlocking();
                if (success) {
                    reconnectAttempts = 0;
                } else {
                    attemptReconnect();
                }
            } catch (Exception e) {
                if (e instanceof IllegalStateException &&
                        e.getMessage().contains("WebSocketClient objects are not reuseable")) {
                    return;
                }

                failedToReconnect = true;
                Astralis.LOGGER.error("foamea");

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
        }, 2, TimeUnit.SECONDS);
    }
}