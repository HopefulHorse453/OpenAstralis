package cc.astralis.module.impl.combat;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.EntityInteractEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;

public class NoMissDelayModule extends Module {
    public final BooleanProperty attackcooldown = new BooleanProperty("Removes Attack Cooldown",true);
    public final BooleanProperty attackonMiss = new BooleanProperty("Removes your attack on miss",true);
    public final BooleanProperty weaponOnly = new BooleanProperty("Weapon Only", true);
    public final BooleanProperty allowBlockHit = new BooleanProperty("Allow Block Hit", false);
    public final BooleanProperty allowAirHit = new BooleanProperty("Allow Air Hit", false);

    public NoMissDelayModule() {
        super(Category.COMBAT);
        registerProperties(attackcooldown, attackonMiss, weaponOnly, allowBlockHit, allowAirHit);
    }
}
