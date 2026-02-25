package cc.astralis.module.impl.movement.speed;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.PlayerUtil;

public class CubecraftSpeed extends SubModule {
    private final NumberProperty cubecraftSpeed = new NumberProperty("Speed", 1, 0, 10, 0.05f);

    public CubecraftSpeed(Module parentClass) {
        super(parentClass, "Cubecraft");
        this.registerPropertyToParentClass(cubecraftSpeed);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (!MoveUtil.isMoving())
            return;

        if (mc.player.onGround())
            mc.player.jumpFromGround();

        if (!mc.player.horizontalCollision) {
            switch (offGroundTicks) {
                case 1 -> PlayerUtil.setMotionY(PlayerUtil.getMotionY() - 0.5);
                case 5 -> PlayerUtil.setMotionY(PlayerUtil.getMotionY() - 0.4);
            }
        }

        if (mc.player.hurtTime != 0)
            MoveUtil.strafe(cubecraftSpeed.getProperty().floatValue());
        else {
            MoveUtil.strafe(mc.player.onGround() ? 0.55 : MoveUtil.getBaseSpeed());
        }
    }
}
