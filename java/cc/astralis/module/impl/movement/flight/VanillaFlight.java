package cc.astralis.module.impl.movement.flight;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.PlayerUtil;

public class VanillaFlight extends SubModule {
    private final NumberProperty speed = new NumberProperty("Speed", 1, 0, 10, 0.1f);

    public VanillaFlight(Module parentClass) {
        super(parentClass, "Vanilla");
        this.registerPropertyToParentClass(speed);
    }

    // todo: white list from obfuscation.
    @EventTarget
    public void onMotion(MotionEvent event) {
        PlayerUtil.setMotionY((mc.options.keyJump.isDown() ?
                speed.getProperty().floatValue() * 0.6 :
                mc.options.keyShift.isDown() ? -speed.getProperty().floatValue() * 0.6 : 0));

        MoveUtil.strafe(speed.getProperty().floatValue());
    }
}
