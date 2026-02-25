package cc.astralis.module.impl.movement.speed;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.input.InputTickEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.movement.ScaffoldRecodeModule;

public class LegitSpeed extends SubModule {

    public LegitSpeed(Module parentClass) {
        super(parentClass, "Legit");
    }

    @EventTarget
    public void onUpdate(InputTickEvent event) {
        if (Astralis.getInstance().getModuleManager().getModule(ScaffoldRecodeModule.class).isToggled())
            return;

       event.jump = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
