package cc.astralis.module.impl.player;

import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;

public class NoPushModule extends Module {
    public final BooleanProperty playerNoPush = new BooleanProperty("Player No Push", true);
    public final BooleanProperty waterNoPush = new BooleanProperty("Water No Push", false);
    public NoPushModule() {
        super(Category.PLAYER);
        registerProperties(playerNoPush, waterNoPush);
    }
}
