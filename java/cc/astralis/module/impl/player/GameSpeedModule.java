package cc.astralis.module.impl.player;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.Data;

public class GameSpeedModule extends Module {
    private final ModeProperty mode = new ModeProperty("Mode","Custom", "Normal","Custom");
    private final NumberProperty timer = new NumberProperty("Timer", 1, 0, 10, 0.1f).setVisible(() -> mode.is("Normal"));
    private final NumberProperty groundTimer = new NumberProperty("Ground Timer", 1, 0, 10, 0.1f).setVisible(() -> mode.is("Custom"));
    private final NumberProperty airTimer = new NumberProperty("Air Timer", 1, 0, 10, 0.1f).setVisible(() -> mode.is("Custom"));

    public GameSpeedModule() {
        super(Category.EXPLOIT);
        registerProperties(mode, timer, groundTimer, airTimer);
    }

    @Override
    public void onDisable() {
        Data.timer = 1f;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        switch (mode.getProperty()) {
            case "Normal" -> Data.timer = timer.getProperty().floatValue();
            case "Custom" -> {
                if (mc.player.onGround()) {
                    Data.timer = groundTimer.getProperty().floatValue();
                } else {
                    Data.timer = airTimer.getProperty().floatValue();
                }
            }
        }
    }
}
