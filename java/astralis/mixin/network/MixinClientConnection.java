
package astralis.mixin.network;

import cc.astralis.Astralis;
import cc.astralis.interfaces.access.IClientConnection;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import io.netty.channel.Channel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.RejectedExecutionException;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.RunningOnDifferentThreadException;

@Mixin(Connection.class)
public abstract class MixinClientConnection implements IClientConnection {

    @Shadow
    public abstract void send(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener);

    @Shadow
    private Channel channel;

    @Shadow
    private static <T extends PacketListener> void genericsFtw(Packet<T> packet, PacketListener listener) {}

    @Shadow
    private PacketListener packetListener;

    @Shadow public abstract void disconnect(Component disconnectReason);
    @Shadow public abstract PacketFlow getReceiving();

    /**
     * @author niceto
     * @reason cuz why not
     */
    @Overwrite
    public void send(Packet<?> packet) {
        final PacketEvent event = new PacketEvent(packet, EventModes.SEND);
        Astralis.getInstance().getEventManager().call(event);

        if (event.isCancelled())
            return;

        packet = event.getPacket();

        this.send(packet, null);
    }

    @Override
    public void sendNoEvent(Packet<?> packet) {
        this.send(packet, null);
    }

    @Override
    public void sendWithEvent(Packet<?> packet) {
        this.send(packet);
    }

    @Override
    public void receiveNoEvent(Packet<?> packet) {
        if (this.channel.isOpen()) {
            try {
                genericsFtw(packet, this.packetListener);
            } catch (RunningOnDifferentThreadException ignored) {
            } catch (RejectedExecutionException var5) {
                this.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
            } catch (ClassCastException var6) {
                //LOGGER.error("Received {} that couldn't be processed", packet.getClass(), var6);
                this.disconnect(Component.translatable("multiplayer.disconnect.invalid_packet"));
            }
        }
    }

    /**
     * @author niceto
     * @reason cuz why not
     */
    @Overwrite
    public void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
        if (this.channel.isOpen()) {
            try {
                final PacketEvent event = new PacketEvent(packet, EventModes.RECEIVE);

                Astralis.getInstance().getEventManager().call(event);

                if (event.isCancelled())
                    return;

                packet = event.getPacket();

                genericsFtw(packet, this.packetListener);
            } catch (RunningOnDifferentThreadException ignored) {
            } catch (RejectedExecutionException var5) {
                this.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
            } catch (ClassCastException var6) {
               // LOGGER.error("Received {} that couldn't be processed", packet.getClass(), var6);
                this.disconnect(Component.translatable("multiplayer.disconnect.invalid_packet"));
            }
        }
    }
}
