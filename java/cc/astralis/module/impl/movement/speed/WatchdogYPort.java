package cc.astralis.module.impl.movement.speed;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MoveEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.util.player.MoveUtil;

public class WatchdogYPort extends SubModule {

    public WatchdogYPort(Module parentClass) {
        super(parentClass,"Watchdog Yport");
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        if (mc.player.fallDistance > 0.07)
            MoveUtil.stop();

        if (mc.player.onGround()) event.setY(0.003);
        if (mc.player.onGround() || mc.player.getDeltaMovement().y == (0.003 - 0.08) * 0.98F)
            MoveUtil.strafe(event, MoveUtil.getPerfectValue(0.3296F, 0.3689F, 0.4252F));
    }
}
