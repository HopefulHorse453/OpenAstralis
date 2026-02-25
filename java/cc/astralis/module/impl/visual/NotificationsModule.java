package cc.astralis.module.impl.visual;

import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ModeProperty;

public class NotificationsModule extends Module {
    public final BooleanProperty displayNotificationOnToggle = new BooleanProperty("Notification on Toggle", true);
    public final ModeProperty mode = new ModeProperty("Mode", "Modern", "Modern", "Legacy");
    public final BooleanProperty popSound = new BooleanProperty("Pop Sound", true);

    public NotificationsModule() {
        super(Category.VISUAL);
        this.registerProperties(mode, displayNotificationOnToggle, popSound);
    }
}
