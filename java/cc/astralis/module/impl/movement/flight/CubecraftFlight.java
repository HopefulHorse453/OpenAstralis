package cc.astralis.module.impl.movement.flight;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.BlockShapeEvent;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.PlayerUtil;
import cc.astralis.util.render.ChatUtil;

public class CubecraftFlight extends SubModule {
    private final NumberProperty speed = new NumberProperty("Speed", 1, 0, 10, 0.1f);
    private final TimeUtil verusTime = new TimeUtil();
    private final TimeUtil timeFromDmg = new TimeUtil();
    private boolean damaged = false;
    private int clips = 0;

    public CubecraftFlight(Module parentClass) {
        super(parentClass, "Cube Craft");
        this.registerPropertyToParentClass(speed);
    }

    @Override
    public void onEnable() {
        mc.player.setPosRaw(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        mc.player.setPosRaw(mc.player.getX(), mc.player.getY() + 3.4, mc.player.getZ());
        damaged = false;
        clips = 0;
        super.onEnable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (!damaged)
            MoveUtil.stop();

        long delayAfterHit = 1000;
        long moveDelayMs = 100;
        float clipAmount = 1;

        if (mc.player.hurtTime != 0 && clips < 2) {
            if (!damaged) {
                damaged = true;
                timeFromDmg.reset();
                ChatUtil.printDebug("Took damage, waiting " + delayAfterHit + "ms...");
                clips++;
            }

            ChatUtil.printDebug("boost");
            MoveUtil.strafe(0.5);
            PlayerUtil.setMotionY(0.5);
        }

        if (damaged && timeFromDmg.finished(delayAfterHit) && !mc.player.onGround()) {
            if (mc.player.fallDistance > 2) {
                mc.player.fallDistance = 0;
            }

            if (mc.player.tickCount % 5 == 0) {
                PlayerUtil.setMotionY(-0);
            }

            event.setOnGround(true);
            MoveUtil.strafe();
        }
    }

    @EventTarget
    public void onCollision(BlockShapeEvent event) {

    }
}