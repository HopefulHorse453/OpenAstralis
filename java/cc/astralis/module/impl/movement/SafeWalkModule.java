package cc.astralis.module.impl.movement;

import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;


public class SafeWalkModule extends Module {
    public final BooleanProperty blocksOnly = new BooleanProperty("Blocks only", false);
    public final BooleanProperty pitchCheck = new BooleanProperty("Pitch check", false);
    public final BooleanProperty backwards = new BooleanProperty("Backwards Only", false);

    public SafeWalkModule() {
        super(Category.MOVEMENT);
        registerProperties(blocksOnly, pitchCheck,backwards);
    }
}
