package cc.astralis.module.impl.combat.velocity;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MoveEvent;
import cc.astralis.event.events.impl.game.StrafeEvent;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.network.PacketUtil;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

// thanks balls.
public class GrimVelocity extends SubModule {
    private final NumberProperty value = new NumberProperty("Grim Value", 3,1 , 10, 1);

    public GrimVelocity(Module parentClass) {
        super(parentClass, "Grim");
        registerPropertyToParentClass(value);
    }

    private int anInt;
    private Vec3 motion;

    @EventTarget
    public void onMove(MoveEvent event) {
        if (anInt > 0) {
            event.setCancelled(true);
            anInt--;
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (event.getEventMode() == EventModes.PRE) {
            if (anInt > 0) {
                event.setCancelled(true);
                if (motion == null) {
                    motion = get();
                }
            } else if (motion != null) {
                set(motion);
                motion = null;
            }
        }
    }

    @EventTarget
    public void onUpdate(TickEvent event) {
        if (anInt > 0) {
            PacketUtil.sendNoEvent(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(mc.player.position().with(Direction.Axis.Y,
                    mc.player.getBlockY()), Direction.UP, mc.player.blockPosition().below(), false), 0));
        }
    }

    @EventTarget
    public void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof ClientboundSetEntityMotionPacket packet && packet.getId() == mc.player.getId()) {
            e.setCancelled(true);
            anInt = value.getProperty().intValue();
        }
    }

    private static Vec3 get() {
        return mc.player != null ? mc.player.getDeltaMovement() : Vec3.ZERO;
    }

    private static void set(Vec3 vec3) {
        if (mc.player != null) mc.player.setDeltaMovement(vec3);
    }
}
