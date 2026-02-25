package cc.astralis.module.impl.movement;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.util.player.MoveUtil;

public class SprintModule extends Module {
    public SprintModule() {
        super(Category.MOVEMENT);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (Astralis.getInstance().getModuleManager().getModule(ScaffoldRecodeModule.class).isToggled()) {
            return;
        }

        mc.options.keySprint.setDown(MoveUtil.isMoving());
    }
}
