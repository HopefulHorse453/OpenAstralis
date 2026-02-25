package cc.astralis.module.impl.movement.speed;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import astralis.mixin.accessor.entity.LivingEntityAccessor;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.PlayerUtil;

public class VulcanGroundDeprecatedSpeed extends SubModule {
    private final TimeUtil timeUtil = new TimeUtil();

    public VulcanGroundDeprecatedSpeed(Module parentClass) {
        super(parentClass,"Vulcan Ground");
    }

    @Override
    public void onEnable() {
        this.timeUtil.reset();
        super.onEnable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        final int boostDelay = 100;
        /*    mc.player.swingHand(Hand.MAIN_HAND);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(mc.crosshairTarget.getPos(), Direction.DOWN, mc.player.getBlockPos().down(), false));
*/
        if (MoveUtil.isMoving()) {
            if (mc.player.onGround()) {
                ((LivingEntityAccessor) mc.player).setNoJumpDelay(2);

                float value = 0.00788f;
                PlayerUtil.setMotionY(value);
                event.setY(event.getY() + value);
                MoveUtil.strafe(MoveUtil.getBaseSpeed() * MoveUtil.getPerfectValue(1.712f,
                        1.712f, !timeUtil.finished(boostDelay) ? 1.46f : 1.8f));
            } else if (offGroundMotionTicks == 1) {
                MoveUtil.strafe(MoveUtil.getBaseSpeed() * MoveUtil.getPerfectValue(1.0407f,
                        1.0407f, !timeUtil.finished(boostDelay) ? 1.11f : 1.45f));
            }
        }
    }
}
