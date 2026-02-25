package cc.astralis.module.impl.movement.speed;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.util.player.MoveUtil;

public class MushSpeed extends SubModule {
    public MushSpeed(Module parentClass) {
        super(parentClass,"Mush");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround())
            mc.player.jumpFromGround();
        else
            MoveUtil.strafe();

        /*if (mc.player.hurtTime != 0) {
            PlayerUtil.setMotionY(PlayerUtil.getMotionY() - 0.80);
        }*/
    }
}

