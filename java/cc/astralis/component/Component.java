package cc.astralis.component;

import cc.astralis.Astralis;
import cc.astralis.interfaces.Fonts;
import cc.astralis.interfaces.IAccess;
import cc.astralis.util.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Component extends Data implements IAccess, Fonts {
    public static boolean activate;

    public Component() {
        onEnable();
    }

    public void onEnable() {
        Astralis.getInstance().getEventManager().register(this);
    }

    public void onDisable() {
        Astralis.getInstance().getEventManager().unregister(this);
    }
}
