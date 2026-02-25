package club.serenityutils.packets;

import cc.astralis.Astralis;
import cc.astralis.util.io.ProtectionUtil;
import club.serenityutils.manager.AbstractRegistryManager;
import club.serenityutils.packets.api.IPacket;
import club.serenityutils.packets.api.manager.IPacketManager;
import club.serenityutils.packets.impl.*;
import club.serenityutils.packets.impl.cloud.AddCloudConfigPacket;
import club.serenityutils.packets.impl.cloud.DeleteCloudConfigPacket;
import club.serenityutils.packets.impl.cloud.FetchCloudConfigsPacket;
import club.serenityutils.packets.impl.cloud.FetchCloudConfigsResponsePacket;
import club.serenityutils.packets.impl.module.FetchModuleInfoPacket;
import club.serenityutils.packets.impl.module.FetchModuleInfoResponsePacket;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ThreadLocalRandom;

public class PacketManager extends AbstractRegistryManager<Integer, Class<? extends IPacket>> implements IPacketManager {
    public PacketManager() {
        registerAll(
                AuthPacket.class,
                MessagePacket.class,
                ClientKeepAlivePacket.class,
                ServerKeepAlivePacket.class,
                HandShakePacket.class,
                BroadcastMessagePacket.class,
                DataTransferPacket.class,
                FetchModuleInfoPacket.class,
                FetchModuleInfoResponsePacket.class,
                FetchCloudConfigsPacket.class,
                FetchCloudConfigsResponsePacket.class,
                AddCloudConfigPacket.class,
                DeleteCloudConfigPacket.class,
                GlobalIRCMessagePacket.class
        );
    }

    @SafeVarargs
    private void registerAll(Class<? extends Packet>... packets) {
        for (Class<? extends Packet> packetClass : packets) {
            try {
                Packet instance = packetClass.getDeclaredConstructor().newInstance();
                int id = instance.getId();
                register(id, packetClass);
            } catch (Exception e) {
                Astralis.LOGGER.error("packet");

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

    @Override
    public IPacket createPacket(int id) throws Exception {
        Class<? extends IPacket> clazz = get(id);

        if (clazz == null)
            throw new IllegalArgumentException("Unknown packet ID: " + id);

        return clazz.getDeclaredConstructor().newInstance();
    }
}