package cc.astralis.module.impl.movement.flight;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.player.PlayerUtil;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.world.level.block.SlimeBlock;

public class VulcanFlight extends SubModule {
    private final TimeUtil timeUtil = new TimeUtil();

    public VulcanFlight(Module parentClass) {
        super(parentClass, "Vulcan");
    }

    @Override
    public void onEnable() {
        if (!(mc.level.getBlockState(mc.player.blockPosition().below()).getBlock() instanceof SlimeBlock))
            ChatUtil.print("You need to be on a slime block to use this flight.");

        super.onEnable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround() && mc.level.getBlockState(mc.player.blockPosition().below()).getBlock() instanceof SlimeBlock) {
            PlayerUtil.setMotionY(4);
            timeUtil.reset();
        }

        if (timeUtil.finished(1500)) {
            if (mc.player.fallDistance > 2) {
                mc.player.fallDistance = 0;
            }

            PlayerUtil.setMotionY(mc.player.tickCount % 3 != 0 ? -0.0972 : PlayerUtil.getMotionY() + 0.026);
        }
    }
}
