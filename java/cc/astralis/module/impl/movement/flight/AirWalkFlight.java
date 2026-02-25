package cc.astralis.module.impl.movement.flight;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.BlockShapeEvent;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.property.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;

public class AirWalkFlight extends SubModule {
    private final BooleanProperty spoofGround = new BooleanProperty("Spoof Ground", false);

    public AirWalkFlight(Module parentClass) {
        super(parentClass, "Air Walk");
        this.registerPropertyToParentClass(spoofGround);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (spoofGround.getProperty())
            event.setOnGround(true);
    }

    @EventTarget
    public void onBlockShape(BlockShapeEvent event) {
        if (event.getPos().getY() < mc.player.getBlockY()) {
            event.setShape(Shapes.block());
        }
    }
}
