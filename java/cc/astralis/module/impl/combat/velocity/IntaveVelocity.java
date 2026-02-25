package cc.astralis.module.impl.combat.velocity;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import astralis.mixin.accessor.network.EntityVelocityUpdateS2CPacketAccessor;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public class IntaveVelocity extends SubModule {
    public IntaveVelocity(Module parentClass) {
        super(parentClass, "Intave");
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getEventMode() == EventModes.RECEIVE && event.getPacket() instanceof ClientboundSetEntityMotionPacket velocityPacket) {
            EntityVelocityUpdateS2CPacketAccessor velocityAccessor = ((EntityVelocityUpdateS2CPacketAccessor) event.getPacket());

            if (velocityAccessor.getId() != mc.player.getId()) {
                return;
            }

            if (mc.player.hurtTime > 1) {
                mc.player.setDeltaMovement(
                        velocityPacket.getMovement().x * 0.4,
                        velocityPacket.getMovement().y,
                        velocityPacket.getMovement().z * 0.4
                );
            }
        }
    }
}
