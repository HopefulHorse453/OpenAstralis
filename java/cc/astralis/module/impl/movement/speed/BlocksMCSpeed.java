package cc.astralis.module.impl.movement.speed;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.util.player.MoveUtil;

public class BlocksMCSpeed extends SubModule {
    public BlocksMCSpeed(Module parentClass) {
        super(parentClass,"Blocks MC");
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player.onGround())
            mc.player.jumpFromGround();

      /*  int simpleY = (int) Math.round((mc.player.getY() % 1) * 10000);


        switch (simpleY) {
            case 13 -> PlayerUtil.setMotionY(PlayerUtil.getMotionY() - 0.02483);
            case 2000 -> PlayerUtil.setMotionY(PlayerUtil.getMotionY() - 0.1913);
        }*/
        MoveUtil.strafe(MoveUtil.getSpeed());

    }
}
