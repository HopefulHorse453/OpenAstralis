package cc.astralis.module.impl.combat.velocity;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.network.PacketUtil;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public class DelayVelocity extends SubModule {
    private final NumberProperty delayMs = new NumberProperty("Delay Timeout", 400, 0, 1000, 50);
    private final BooleanProperty releaseOnGround = new BooleanProperty("Release on Ground", true);

    private final ConcurrentLinkedQueue<PacketUtil.TimedPacket> bufferedPackets = new ConcurrentLinkedQueue<>();
    private boolean delaying;
    private boolean keepAlive;

    public DelayVelocity(Module parent) {
        super(parent, "Delay");
        registerPropertyToParentClass(delayMs);
        registerPropertyToParentClass(releaseOnGround);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getEventMode() == EventModes.SEND) {
            return;
        }

        Packet<?> packet = event.getPacket();

        if (packet instanceof ClientboundKeepAlivePacket) {
            keepAlive = true;
        }

        if (packet instanceof ClientboundSetEntityMotionPacket velocityPacket) {
            if (velocityPacket.getId() == mc.player.getId()) {
                if (keepAlive) {
                    keepAlive = false;
                    return;
                }
                if (conditionals()) {
                    delaying = true;
                }
            }
        }

        if (!delaying) {
            return;
        }

        if (!(packet instanceof ClientboundSetEntityMotionPacket || packet instanceof ClientboundPingPacket || packet instanceof ClientboundKeepAlivePacket)) {
            return;
        }

        bufferedPackets.add(new PacketUtil.TimedPacket(packet, System.currentTimeMillis()));
        event.setCancelled(true);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (bufferedPackets.isEmpty()) {
            return;
        }

        boolean canFlush = false;

        for (PacketUtil.TimedPacket packet : bufferedPackets) {
            if (System.currentTimeMillis() - packet.time() >= delayMs.getProperty().floatValue()) {
                canFlush = true;
                break;
            }
        }

        if (canFlush) {
            flushOne();
        }

        if (releaseOnGround.getProperty() && mc.player.onGround() || !containsVelocity() || mc.player == null) {
            flushAll();
        }
    }

    private void flushOne() {
        PacketUtil.TimedPacket packet = bufferedPackets.poll();
        if (packet != null) {
            PacketUtil.receiveNoEvent(packet.packet());
        }
    }

    private void flushAll() {
        while (!bufferedPackets.isEmpty()) {
            flushOne();
        }
        delaying = false;
    }

    private boolean conditionals() {
        if (mc.player == null) return false;
        if (mc.player.getAbilities().flying) return false;
        return true;
    }

    private boolean containsVelocity() {
        for (PacketUtil.TimedPacket packet : bufferedPackets) {
            if (packet.packet() instanceof ClientboundSetEntityMotionPacket velocityPacket) {
                if (velocityPacket.getId() == mc.player.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        bufferedPackets.clear();
        delaying = false;
        keepAlive = false;
        super.onDisable();
    }
}