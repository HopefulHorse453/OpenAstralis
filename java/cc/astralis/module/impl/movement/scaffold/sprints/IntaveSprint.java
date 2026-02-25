package cc.astralis.module.impl.movement.scaffold.sprints;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;

public class IntaveSprint extends SubModule {
    public IntaveSprint(Module parentClass) {
        super(parentClass,"Intave");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround()) {
            final float multiply = 1.1f;
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x * multiply, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z * multiply);
        }
    }
}
