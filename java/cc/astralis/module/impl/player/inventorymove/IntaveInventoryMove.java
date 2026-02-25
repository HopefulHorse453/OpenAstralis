package cc.astralis.module.impl.player.inventorymove;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.player.InventoryMoveModule;
import net.minecraft.client.KeyMapping;

public class IntaveInventoryMove extends SubModule {
    private final InventoryMoveModule parentClass;

    public IntaveInventoryMove(Module parentClass) {
        super(parentClass, "Intave");
        this.parentClass = (InventoryMoveModule) parentClass;
    }

    @EventTarget
    public void onUpdate(UpdateEvent e) {
        if (mc.screen == null || !parentClass.ScreenCheck()) {
            return;
        }

        KeyMapping.setAll();

        mc.options.keyShift.setDown(true);
        mc.options.keyJump.setDown(false);
    }
}
