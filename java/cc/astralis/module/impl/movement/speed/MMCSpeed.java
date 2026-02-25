package cc.astralis.module.impl.movement.speed;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.util.player.MoveUtil;

public class MMCSpeed extends SubModule {
    public MMCSpeed(Module parentClass) {
        super(parentClass, "MMC");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround()){
            mc.player.jumpFromGround();
        }

        if (offGroundTicks == 10 && mc.player.hurtTime == 0) {
            MoveUtil.strafe();
        }
    }
}