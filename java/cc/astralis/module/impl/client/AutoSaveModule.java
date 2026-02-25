package cc.astralis.module.impl.client;

import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.NumberProperty;

public class AutoSaveModule extends Module {
    public final BooleanProperty autoSaveConfig = new BooleanProperty("Auto Save Config", true);
    public final NumberProperty autoSaveConfigDelay = new NumberProperty("Auto Save Config Delay", 300000, 1000, 3600000f, 1000);

    public AutoSaveModule() {
        super(Category.EXPLOIT);
    }
}
