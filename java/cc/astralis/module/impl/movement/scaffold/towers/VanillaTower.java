package cc.astralis.module.impl.movement.scaffold.towers;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.movement.ScaffoldWalkModule;

public class VanillaTower extends SubModule {
    public VanillaTower(Module parentClass) {
        super(parentClass,"Vanilla");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (!mc.options.keyJump.isDown() || !((ScaffoldWalkModule) getParentClass()).tower.getProperty()) {
            return;
        }
           // mc.player.setVelocity(mc.player.getVelocity().x, 0.42f, mc.player.getVelocity().z);

    }
}