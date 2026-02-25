package cc.astralis.module.impl.combat;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.EntityInteractEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Category;
import cc.astralis.module.Module;

public class ForceCritModule extends Module {

    public ForceCritModule() {
        super(Category.COMBAT);
    }

    @EventTarget
    public void onAttack(EntityInteractEvent event) {
        if (event.getEventMode() == EventModes.PRE && !mc.player.onGround()) {
            mc.player.setSprinting(false);
        }
    }
}
