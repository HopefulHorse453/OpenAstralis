package cc.astralis.module.impl.movement.scaffold.sprints;

import cc.astralis.Astralis;
import cc.astralis.component.impl.network.BlinkComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.movement.ScaffoldWalkModule;
import cc.astralis.util.math.TimeUtil;

public class ModernWatchdogSprint extends SubModule {
    private final ScaffoldWalkModule sc = (ScaffoldWalkModule) getParentClass();
    private final TimeUtil timeUtil = new TimeUtil();
    private final BlinkComponent blinkComponent = Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class);
    private boolean hasBlinked = false;

    public ModernWatchdogSprint(Module parentClass) {
        super(parentClass, "Modern Watchdog");
    }

    @Override
    public void onDisable() {
        blinkComponent.stopBlinking();
        timeUtil.reset();
        hasBlinked = false;
        super.onDisable();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!hasBlinked) {
            blinkComponent.startBlinking();
            timeUtil.reset();
            hasBlinked = true;
        } else if (timeUtil.finished(1000)) {
            blinkComponent.stopBlinking();
        }
    }
}

