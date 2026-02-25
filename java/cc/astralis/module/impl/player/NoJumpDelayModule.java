package cc.astralis.module.impl.player;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import astralis.mixin.accessor.entity.LivingEntityAccessor;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.movement.ScaffoldRecodeModule;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.NumberProperty;

public class NoJumpDelayModule extends Module {
    private final NumberProperty delay = new NumberProperty("Delay", 0, 0, 4, 1);
    public final BooleanProperty notWhileScaffold = new BooleanProperty("Not while Scaffold", true);

    public NoJumpDelayModule() {
        super(Category.PLAYER);
        registerProperties(delay, notWhileScaffold);
    }

    @EventTarget
    public void onUpdate(UpdateEvent e) {
        if (mc.player == null || !mc.player.onGround()) {
            return;
        }

        if (notWhileScaffold.getProperty() && (Astralis.getInstance().getModuleManager().getModule(ScaffoldRecodeModule.class).isToggled())) {
            return;
        }
        ((LivingEntityAccessor) mc.player).setNoJumpDelay(delay.getProperty().intValue());
    }
}