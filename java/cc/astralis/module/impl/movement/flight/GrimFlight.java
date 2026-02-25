package cc.astralis.module.impl.movement.flight;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.event.events.impl.game.StrafeEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.util.network.PacketUtil;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.PlayerUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

// thanks balls.
public class GrimFlight extends SubModule {

    public GrimFlight(Module parentClass) {
        super(parentClass, "Grim");
    }

    private boolean waiting, motion;


    @Override
    public void onEnable() {
        waiting = false;
        motion = false;
        super.onEnable();
    }

    @EventTarget
    public void onStrafe(StrafeEvent e) {
        if (e.getEventMode() == EventModes.POST) {
            if (!waiting) {
                if (mc.player.fallDistance > 0.0) {
                    PacketUtil.sendNoEvent(new ServerboundMovePlayerPacket.StatusOnly(true, false));
                    mc.player.fallDistance = 0.0;
                    waiting = true;
                }
            }
            if (waiting) {
                set(Vec3.ZERO);
            }
            if (motion) {
                MoveUtil.strafe(0.3f);
                PlayerUtil.setMotionY(-0.0002);
                motion = false;
            }
        }
    }

    @EventTarget
    public void onMotion(MotionEvent e) {
        if (waiting) {
            e.setCancelled(true);
        }
    }

    @EventTarget
    public void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof ClientboundSetEntityMotionPacket packet && packet.getId() == mc.player.getId()) {
            waiting = false;
            e.setCancelled(true);
            motion = true;
        }
    }

    private static void set(Vec3 vec3) {
        if (mc.player != null) mc.player.setDeltaMovement(vec3);
    }
}
