package cc.astralis.interfaces.access;

import net.minecraft.network.protocol.Packet;

public interface IClientConnection {

    void sendNoEvent(Packet<?> packet);
    void sendWithEvent(Packet<?> packet);
    void receiveNoEvent(Packet<?> packet);

}
