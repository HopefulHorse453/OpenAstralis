package cc.astralis.module.impl.movement.speed;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.event.events.impl.game.MoveEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.util.math.RandomUtil;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.PlayerUtil;
import cc.astralis.util.render.ChatUtil;

public class VulcanStrafeDeprecatedSpeed extends SubModule {
    private final BooleanProperty boost = new BooleanProperty("Boost", true),
            glitchBoost = new BooleanProperty("Glitch Boost", false),
            glide = new BooleanProperty("Glide", false),
            devGlide = new BooleanProperty("Dev Glide", false);

    private boolean groundSpoof;
    private final TimeUtil timeUtil = new TimeUtil();

    public VulcanStrafeDeprecatedSpeed(Module parentClass) {
        super(parentClass,"Vulcan Strafe");
        this.registerPropertiesToParentClass(boost, glitchBoost, glide, devGlide);
    }

    @Override
    public void onEnable() {
        timeUtil.reset();
        super.onEnable();
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        //can go down to 4 but sometimes flags and if u wanna target strafe u need 6 ticks
        if (offGroundTicks > 5 && MoveUtil.isMoving()) {
            MoveUtil.strafe(MoveUtil.getPerfectValue(1, 1, boost.getProperty() ? 0.6312f : RandomUtil.getAdvancedRandom(0.40f, 0.45f)));
        }

        groundSpoof = offGroundTicks > 6;
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.fallDistance > 2) {
            ChatUtil.printDebug("returning");
            groundSpoof = false;
            return;
        }

        if (glide.getProperty() && offGroundTicks > 6 && offGroundTicks < (!devGlide.getProperty() ? 16 : 20)) {
            PlayerUtil.setMotionY(mc.player.tickCount % 3 != 0 ? -0.0991 : (!devGlide.getProperty() ? mc.player.getDeltaMovement().y : 0) + 0.026);
        }

        if (groundSpoof) {
            event.setOnGround(true);
        }

        if (mc.player.onGround()) {
            //1.3 / 1.5
            // before patch values RandomUtil.getAdvancedRandom(1.8f, 2f)
            mc.player.jumpFromGround();
            MoveUtil.strafe(MoveUtil.getBaseSpeed() * RandomUtil.getAdvancedRandom(1.2f, 1.5f));
        }

        //semi stable :3
        if (glitchBoost.getProperty()) {
            mc.options.keyJump.setDown(!timeUtil.finished((500)));

            if (timeUtil.finished(1000)) {
                timeUtil.reset();
            }
        }
    }
}
