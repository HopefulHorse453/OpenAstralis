package cc.astralis.module.impl.movement.flight;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.util.player.MoveUtil;
import net.minecraft.core.Direction;

public class VerusFlight extends SubModule {

    public VerusFlight(Module parentClass) {
        super(parentClass, "Verus Glide");
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        mc.player.setDeltaMovement(mc.player.getDeltaMovement().with(Direction.Axis.Y, -0.078400001525878));
        if(!mc.player.onGround())
            MoveUtil.strafe(0.37);
    }
}
