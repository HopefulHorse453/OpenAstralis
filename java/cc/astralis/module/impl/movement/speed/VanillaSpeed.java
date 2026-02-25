package cc.astralis.module.impl.movement.speed;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.player.MoveUtil;

public class VanillaSpeed extends SubModule {
    private final NumberProperty speed = new NumberProperty("Speed", 1, 0, 10, 0.1f);

    public VanillaSpeed(Module parentClass) {
        super(parentClass,"Vanilla");
        registerPropertyToParentClass(speed);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
        }

        MoveUtil.strafe(speed.getProperty().floatValue());
    }
}