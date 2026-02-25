package cc.astralis.module.impl.movement;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.movement.flight.*;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ClassModeProperty;
import cc.astralis.util.player.MoveUtil;

public class FlightModule extends Module {
    private final ClassModeProperty classModeProperty = new ClassModeProperty("Mode",
            new AirWalkFlight(this), new BlockFlight(this),
            new VanillaFlight(this), new VulcanFlight(this),
            new CubecraftFlight(this), new GrimFlight(this),
            new VerusFlight(this)
    );
    private final BooleanProperty stopOnDisable = new BooleanProperty("Stop on Disable",true);

    public FlightModule() {
        super(Category.MOVEMENT);
        registerProperties(classModeProperty,
                stopOnDisable
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable(){
        if (stopOnDisable.getProperty())
            MoveUtil.stop();

        super.onDisable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        this.setSuffix(classModeProperty.getProperty().getName());
    }
}