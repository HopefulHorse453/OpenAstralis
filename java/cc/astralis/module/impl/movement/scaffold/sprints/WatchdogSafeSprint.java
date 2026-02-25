package cc.astralis.module.impl.movement.scaffold.sprints;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.movement.ScaffoldWalkModule;
import cc.astralis.module.impl.movement.SpeedModule;
import cc.astralis.util.player.MoveUtil;

public class WatchdogSafeSprint extends SubModule {
    private final ScaffoldWalkModule sc = (ScaffoldWalkModule) getParentClass();

    public WatchdogSafeSprint(Module parentClass) {
        super(parentClass,"Watchdog Safe");
    }
    //so this class is useless either u redo it or remove it

    @EventTarget
    public void onMotion(MotionEvent event) {
        SpeedModule speedModule = Astralis.getInstance().getModuleManager().getModule(SpeedModule.class);

        if (mc.options.keyJump.isDown()) {
            return;
        }

        if (MoveUtil.isGoingDiagonally()) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x * 0.98, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z * 0.98);
        }

        if (MoveUtil.isPressingForwardAndStrafe()) {
            if (speedModule.isToggled())
                speedModule.toggle();
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x * 0.5, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z * 0.5);
        }
    }
}
