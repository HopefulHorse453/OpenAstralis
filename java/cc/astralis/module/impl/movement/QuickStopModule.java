package cc.astralis.module.impl.movement;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.util.player.MoveUtil;

public class QuickStopModule extends Module {

    public QuickStopModule() {
        super(Category.MOVEMENT);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (!MoveUtil.isMoving())
            MoveUtil.stop();
    }
}
