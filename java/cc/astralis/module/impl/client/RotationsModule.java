package cc.astralis.module.impl.client;

import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;

public class RotationsModule extends Module {
    public final BooleanProperty movementCorrection = new BooleanProperty("Movement Correction", false),
        oldHitBoxOffset = new BooleanProperty("Old Hit Box Offset", false),
        modernHitVec = new BooleanProperty("Modern Hit Vec", true);

    public RotationsModule() {
        super(Category.PLAYER);
        this.registerProperties(movementCorrection, oldHitBoxOffset, modernHitVec);
    }
}
