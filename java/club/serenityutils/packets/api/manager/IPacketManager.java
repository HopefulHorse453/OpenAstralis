package club.serenityutils.packets.api.manager;

import club.serenityutils.manager.api.IRegistryManager;
import club.serenityutils.packets.api.IPacket;

public interface IPacketManager extends IRegistryManager<Integer, Class<? extends IPacket>> {
    IPacket createPacket(int id) throws Exception;
}
