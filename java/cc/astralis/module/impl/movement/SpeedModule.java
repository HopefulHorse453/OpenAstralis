package cc.astralis.module.impl.movement;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.movement.speed.*;
import cc.astralis.property.properties.ClassModeProperty;

public class SpeedModule extends Module {
    public final ClassModeProperty speeds = new ClassModeProperty(
            "Speed Modes",
            new VanillaSpeed(this), new StrafeSpeed(this),
            new MMCSpeed(this), new CubecraftSpeed(this),
            new BlocksMCSpeed(this), new MushSpeed(this),
         /*   new WatchdogSpeed(this), new WatchdogSpeed2(this),
            new WatchdogYPort(this),*/ new VulcanStrafeDeprecatedSpeed(this),
            new VulcanGroundDeprecatedSpeed(this), new VulcanLowSpeed(this),
            new LegitSpeed(this)
    );

    public SpeedModule() {
        super(Category.MOVEMENT);
        registerProperty(speeds);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        timer = 1f;
        if (!speeds.is("Watchdog"))
            super.onDisable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        this.setSuffix(speeds.getProperty().getFormatedName());

        if (speeds.is("Watchdog") && !this.isToggled() && ( mc.player.onGround() || offGroundTicks > 9))
            super.onDisable(); // Disable Low hop Properly to prevent flags
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!speeds.is("Vulcan Low"))
            mc.options.keyJump.setDown(false);
    }

}
