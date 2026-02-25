package cc.astralis.module.impl.player;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.player.nofall.*;
import cc.astralis.property.properties.ClassModeProperty;

public class NoFallModule extends Module {

    public final ClassModeProperty mode = new ClassModeProperty("Mode",
            new GroundNofall(this), new UniversalNofall(this),
            new VulcanNofall(this), new WatchdogNofall(this), new BlocksMcNofall(this)
    );

    public NoFallModule() {
        super(Category.PLAYER);
        this.registerProperty(mode);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        this.setSuffix(mode.getProperty().getFormatedName());
    }
}
