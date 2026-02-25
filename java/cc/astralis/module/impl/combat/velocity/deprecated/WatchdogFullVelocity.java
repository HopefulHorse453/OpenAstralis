package cc.astralis.module.impl.combat.velocity.deprecated;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.event.events.impl.game.WorldChangeEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.movement.LongJumpModule;
import cc.astralis.util.network.PacketUtil;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public class WatchdogFullVelocity extends SubModule {
    private final ConcurrentLinkedQueue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private boolean canSpoof;

    public WatchdogFullVelocity(Module parentClass)  {
        super(parentClass,"Watchdog Full");
    }

    @Override
    public void onEnable() {
        super.onEnable();

        for (final Packet<?> packet : this.packets) {
            PacketUtil.receiveNoEvent(packet);
            this.packets.remove(packet);
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getEventMode() == EventModes.POST)
            return;


        if (mc.player == null || mc.player.tickCount <= 1 || mc.player.getAbilities().mayfly || Astralis.getInstance().getModuleManager().getModule(LongJumpModule.class).isToggled()) {
            for (final Packet<?> packet : this.packets) {
                PacketUtil.receiveNoEvent(packet);
                this.packets.remove(packet);
            }

            return;
        }

        if (Astralis.getInstance().getModuleManager().getModule(LongJumpModule.class).isToggled())
            return;
        if (mc.player.onGround() && this.canSpoof) {
            this.canSpoof = false;
            double oldMotionX = mc.player.getDeltaMovement().x;
            double oldMotionY = mc.player.getDeltaMovement().y;
            double oldMotionZ = mc.player.getDeltaMovement().z;

            for (final Packet<?> packet : this.packets) {
                PacketUtil.receiveNoEvent(packet);
                this.packets.remove(packet);
            }

            mc.player.setDeltaMovement(oldMotionX, oldMotionY, oldMotionZ);
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event)  {
        if (event.getEventMode() != EventModes.RECEIVE)
            return;

        if (mc.player == null || mc.player.tickCount <= 1 || mc.player.getAbilities().mayfly || Astralis.getInstance().getModuleManager().getModule(LongJumpModule.class).isToggled()) {
            for (final Packet<?> packet : this.packets) {
                PacketUtil.receiveNoEvent(packet);
                this.packets.remove(packet);
            }

            return;
        }

        if (Astralis.getInstance().getModuleManager().getModule(LongJumpModule.class).isToggled())
            return;

        if (event.getPacket() instanceof ClientboundSetEntityMotionPacket wrapper) {
            if (wrapper.getId() != mc.player.getId()) {
                return;
            }

            this.canSpoof = true;
            this.packets.add(event.getPacket());
            event.setCancelled(true);
        }

        if (event.getPacket() instanceof ClientboundPingPacket && this.canSpoof) {
            this.packets.add(event.getPacket());
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onWorldChange(WorldChangeEvent event) {
        for (final Packet<?> packet : this.packets) {
            PacketUtil.receiveNoEvent(packet);
            this.packets.remove(packet);
        }
    }
}
