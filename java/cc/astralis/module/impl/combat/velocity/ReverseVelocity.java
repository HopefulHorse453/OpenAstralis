package cc.astralis.module.impl.combat.velocity;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import astralis.mixin.accessor.network.EntityVelocityUpdateS2CPacketAccessor;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.velocity.VelocityBuilder;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public class ReverseVelocity extends SubModule {
    private final NumberProperty horizontalVelocity = new NumberProperty("Horizontal Velocity", 0, 0, 100, 1);

    public ReverseVelocity(Module parentClass)  {
        super(parentClass,"Reverse");
        this.registerPropertyToParentClass(horizontalVelocity);
    }

    @EventTarget
    public void onPacket(PacketEvent event)  {
        if (event.getEventMode() == EventModes.RECEIVE && event.getPacket() instanceof ClientboundSetEntityMotionPacket velocityPacket) {
            EntityVelocityUpdateS2CPacketAccessor velocityAccessor = ((EntityVelocityUpdateS2CPacketAccessor) event.getPacket());

            if (velocityAccessor.getId() != mc.player.getId()) {
                return;
            }

            velocityAccessor.setMovement(VelocityBuilder.from(velocityAccessor.getMovement())
                    .setVelocityX(-velocityAccessor.getMovement().x * (horizontalVelocity.getProperty().intValue() / 100D))
                    .setVelocityZ(-velocityAccessor.getMovement().z * (horizontalVelocity.getProperty().intValue() / 100D))
                    .build()
            );
        }
    }
}
