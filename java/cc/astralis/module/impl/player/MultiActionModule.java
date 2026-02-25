package cc.astralis.module.impl.player;

import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;

public class MultiActionModule extends Module {
    public final BooleanProperty placeIfBreaking = new BooleanProperty("Place while mining", false);
    public final BooleanProperty breakIfUsing = new BooleanProperty("Break while using item", false);

    public MultiActionModule() {
        super(Category.PLAYER);
        registerProperties(placeIfBreaking, breakIfUsing);
    }
}
