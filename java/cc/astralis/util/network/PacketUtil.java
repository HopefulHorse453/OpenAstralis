package cc.astralis.util.network;

import cc.astralis.interfaces.access.IClientConnection;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.network.protocol.Packet;
import cc.astralis.interfaces.IAccess;
import astralis.mixin.accessor.player.ClientPlayerInteractionManagerAccessor;

public class PacketUtil implements IAccess {
    public record TimedPacket(Packet<?> packet, long time) {
    }

    public static void receiveNoEvent(Packet<?> packet) {
        if (mc.player == null || mc.player.connection == null)
            return;

        final IClientConnection connection = ((IClientConnection) mc.player.connection.getConnection());
        connection.receiveNoEvent(packet);
    }

    public static void sendNoEvent(Packet<?> packet) {
        if (mc.player == null || mc.player.connection == null)
            return;

        final IClientConnection connection = ((IClientConnection) mc.player.connection.getConnection());
        connection.sendNoEvent(packet);
    }

    public static void send(Packet<?> packet) {
        if (mc.player == null || mc.player.connection == null)
            return;

        final IClientConnection connection = ((IClientConnection) mc.player.connection.getConnection());
        connection.sendWithEvent(packet);
    }

    public static void sendSequenced(PredictiveAction packetCreator) {
        if (mc.level == null) return;
        ((ClientPlayerInteractionManagerAccessor) mc.gameMode).callStartPrediction(mc.level, packetCreator);
    }

    public static void handlePacket(Packet<?> packet) {
        try {
            ((Packet<ClientPacketListener>) packet).handle(mc.getConnection());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
