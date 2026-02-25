package cc.astralis.module.impl.player;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.player.inventorymove.VanillaInventoryMove;
import cc.astralis.module.impl.player.inventorymove.WatchdogInventoryMove;
import cc.astralis.module.impl.player.inventorymove.WatchdogInventoryMove2;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ClassModeProperty;
import cc.astralis.ui.screens.clickgui.dropdown.DropdownCGUIScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public class InventoryMoveModule extends Module {
    public final ClassModeProperty mode = new ClassModeProperty("Mode",
      //      new IntaveInventoryMove(this),
            new VanillaInventoryMove(this),
            new WatchdogInventoryMove(this),
            new WatchdogInventoryMove2(this)
    );
    public final BooleanProperty inventory = new BooleanProperty("Inventory", true);
    public final BooleanProperty chest = new BooleanProperty("Chest", true);
    public final BooleanProperty clickGui = new BooleanProperty("ClickGui", true);
    public final BooleanProperty allowJumping = new BooleanProperty("Allow Jumping", true);
    public final BooleanProperty allowSneaking = new BooleanProperty("Allow Sneaking", false);
    public final BooleanProperty allowSprinting = new BooleanProperty("Allow Sprinting", true);

    public InventoryMoveModule() {
        super(Category.PLAYER);
        this.registerProperties(inventory, chest, mode, allowJumping, allowSneaking, allowSprinting,clickGui);
    }

    @EventTarget
    public void onUpdate(UpdateEvent e) {
        this.setSuffix(mode.getProperty().getFormatedName());
    }

    public boolean ScreenCheck() {
        return (mc.screen instanceof InventoryScreen && inventory.getProperty()) ||
                (mc.screen instanceof ContainerScreen && chest.getProperty()) ||
                (mc.screen instanceof DropdownCGUIScreen && clickGui.getProperty());
    }
}