package cc.astralis.module.impl.combat.velocity;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import astralis.mixin.accessor.network.EntityVelocityUpdateS2CPacketAccessor;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public class CancelVelocity extends SubModule {
    public CancelVelocity(Module parentClass)  {
        super(parentClass,"Cancel");
    }

    @EventTarget
    public void onPacket(PacketEvent event)  {
        if (event.getEventMode() == EventModes.RECEIVE && event.getPacket() instanceof ClientboundSetEntityMotionPacket velocityPacket) {
            EntityVelocityUpdateS2CPacketAccessor velocityAccessor = ((EntityVelocityUpdateS2CPacketAccessor) event.getPacket());

            if (velocityAccessor.getId() != mc.player.getId()) {
                return;
            }

            event.setCancelled(true);
        }
    }
}
