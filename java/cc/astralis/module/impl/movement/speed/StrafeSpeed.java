package cc.astralis.module.impl.movement.speed;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.util.player.MoveUtil;

public class StrafeSpeed extends SubModule {
    public StrafeSpeed(Module parentClass) {
        super(parentClass,"Strafe");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround())
            mc.player.jumpFromGround();

        MoveUtil.strafe(MoveUtil.getBaseSpeed());
    }
}
