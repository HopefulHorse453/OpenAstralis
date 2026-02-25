package cc.astralis.module.impl.player.inventorymove;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.player.InventoryMoveModule;
import net.minecraft.client.KeyMapping;

public class VanillaInventoryMove extends SubModule {
    private final InventoryMoveModule parentClass;

    public VanillaInventoryMove(Module parentClass){
        super(parentClass,"Vanilla");
        this.parentClass = (InventoryMoveModule) parentClass;
    }

    @EventTarget
    public void onUpdate(UpdateEvent e) {
        if (mc.screen == null || !parentClass.ScreenCheck()) {
            return;
        }

        KeyMapping.setAll();

        if (!parentClass.allowJumping.getProperty()) {
            mc.options.keyJump.setDown(false);
        }
        if (!parentClass.allowSneaking.getProperty()) {
            mc.options.keyShift.setDown(false);
        }
        if (!parentClass.allowSprinting.getProperty()) {
            mc.options.keySprint.setDown(false);
            mc.player.setSprinting(false);
        }
    }
}
