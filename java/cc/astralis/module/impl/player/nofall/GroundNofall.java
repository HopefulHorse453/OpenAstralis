package cc.astralis.module.impl.player.nofall;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;

public class GroundNofall extends SubModule {

    public GroundNofall(Module parentClass)  {
        super(parentClass,"Ground");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        event.setOnGround(true);
    }
}
