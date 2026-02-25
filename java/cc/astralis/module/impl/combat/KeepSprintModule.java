package cc.astralis.module.impl.combat;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.LoseSprintEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;

public class KeepSprintModule extends Module {
    private final BooleanProperty groundOnly = new BooleanProperty("Ground Only", false);

    public KeepSprintModule() {
        super(Category.COMBAT);
        this.registerProperty(groundOnly);
    }

    @EventTarget
    public void onLoseSprint(LoseSprintEvent event) {
        if (!groundOnly.getProperty() || mc.player.onGround())
            event.setCancelled(true);
    }
}
