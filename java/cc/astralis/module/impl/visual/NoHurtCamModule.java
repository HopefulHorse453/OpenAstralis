package cc.astralis.module.impl.visual;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.HurtCamEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;

public class NoHurtCamModule extends Module {
    public NoHurtCamModule() {
        super(Category.VISUAL);
    }

    @EventTarget
    public void onHurtCam(HurtCamEvent event) {
        event.setCancelled(true);
    }
}
