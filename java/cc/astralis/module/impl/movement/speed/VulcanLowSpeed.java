package cc.astralis.module.impl.movement.speed;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.PlayerUtil;

// I made this at 2am sry for shit code.
public class VulcanLowSpeed extends SubModule {

    public VulcanLowSpeed(Module parentClass) {
        super(parentClass, "Vulcan Low");
    }


    @EventTarget
    public void onMotion(MotionEvent event) {
        if  (!MoveUtil.isMoving())
            return;

        mc.options.keyJump.setDown(true);
        if (mc.player.onGround()) {
            MoveUtil.strafe();
        }

        if (offGroundTicks <= 2 )
            MoveUtil.strafe(MoveUtil.getSpeed());

        if (offGroundTicks > 3 && mc.player.tickCount % 5 == 0) {
            PlayerUtil.setMotionY(-5);
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x * 0.9, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z * 0.9);
        }
    }
}
