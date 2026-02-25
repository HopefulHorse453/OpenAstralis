package cc.astralis.module.impl.combat;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;

public class SumoBot extends Module {

    public SumoBot() {
        super(Category.COMBAT);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {

    }
}
