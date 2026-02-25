package cc.astralis.module.impl.player;

import cc.astralis.Astralis;
import cc.astralis.component.impl.network.BlinkComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.NumberProperty;

import cc.astralis.util.math.TimeUtil;

public class BlinkModule extends Module {
    private final BooleanProperty flushAutomatically = new BooleanProperty("Flush Automatically", true);
    private final NumberProperty flushIntervalMs = new NumberProperty("Flush Interval (ms)", 500, 100, 2000, 50).setVisible(flushAutomatically::getProperty);

    private final TimeUtil timer = new TimeUtil();

    public BlinkModule() {
        super(Category.PLAYER);
        registerProperties(flushAutomatically, flushIntervalMs);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (!Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).isBlinking())
            return;

        if (flushAutomatically.getProperty() && timer.finished(flushIntervalMs.getProperty().longValue())) {
            BlinkComponent blink = Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class);
            blink.stopBlinking();
            blink.startBlinking();
            timer.reset();
        }
    }

    @Override
    public void onEnable() {
        timer.reset();
        Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).startBlinking();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).stopBlinking();
        super.onDisable();
    }
}
