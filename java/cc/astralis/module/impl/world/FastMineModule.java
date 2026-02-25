package cc.astralis.module.impl.world;

import astralis.mixin.accessor.player.AccessorClientPlayerInteractionManager;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;

public class FastMineModule extends Module {
    private final ModeProperty mode = new ModeProperty("Mode", "Packet", "Packet", "Timer");
    private final NumberProperty speed = new NumberProperty("Speed", 1.25f, 1f, 5f, 0.05f);
    private final BooleanProperty miningCooldown = new BooleanProperty("Mining Cooldown", true);
    private final NumberProperty minningDelay = new NumberProperty("Cooldown", 0, 0, 5, 1).setVisible(miningCooldown::getProperty);

    public FastMineModule() {
        super(Category.WORLD);
        registerProperties(mode, speed, miningCooldown, minningDelay);
    }

    private boolean isTimering = false;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.gameMode == null || mc.player == null || mc.level == null) {
            return;
        }
        this.setSuffix(mode.getProperty() + " " + speed.getProperty().floatValue());

        if (miningCooldown.getProperty() && mc.gameMode instanceof AccessorClientPlayerInteractionManager interactionManager) {
            interactionManager.setBreakingCooldown(minningDelay.getProperty().intValue());
        }

        switch (mode.getProperty()) {
            case "Packet" -> {
                if (mc.gameMode.isDestroying()) {
                    if (((AccessorClientPlayerInteractionManager) mc.gameMode).getCurrentBreakingProgress() >= 1.0F / speed.getProperty().floatValue()) {
                        ((AccessorClientPlayerInteractionManager) mc.gameMode).setCurrentBreakingProgress(1.0F);
                    }
                }
            }
            case "Timer" -> {
                if (mc.gameMode.isDestroying()) {
                    timer = speed.getProperty().floatValue();
                    isTimering = true;
                } else if (isTimering) {
                    timer = 1.0F;
                    isTimering = false;
                }
            }
        }
    }
}

