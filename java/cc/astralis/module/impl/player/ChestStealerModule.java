package cc.astralis.module.impl.player;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.InputProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.player.InventoryUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class ChestStealerModule extends Module {
    private final NumberProperty delay = new NumberProperty("Delay", 100, 0, 500, 1);
    private final BooleanProperty autoClose = new BooleanProperty("Auto Close", true);
    private final BooleanProperty checkTitle = new BooleanProperty("Check title", true);
    private final InputProperty titleSubstring = new InputProperty("Title to check for", "Chest").setVisible(checkTitle::getProperty);

    private long lastStealTime;

    public ChestStealerModule() {
        super(Category.PLAYER);
        registerProperties(delay, autoClose, checkTitle, titleSubstring);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.gameMode == null || mc.level == null) {
            return;
        }

        if (!(mc.screen instanceof ContainerScreen screen)) return;
        var handler = screen.getMenu();

        if (checkTitle.getProperty()) {
            String screenTitle = screen.getTitle().getString();
            if (!screenTitle.toLowerCase().contains(titleSubstring.getProperty().toLowerCase())) {
                return;
            }
        }

        int inventoryStart = handler.slots.size() - 36;
        boolean inventoryFull = true;
        for (int i = inventoryStart; i < handler.slots.size(); i++) {
            if (handler.slots.get(i).getItem().isEmpty()) {
                inventoryFull = false;
                break;
            }
        }

        // Use InventoryUtil to get slots to steal
        InventoryUtil.Info info = InventoryUtil.sort(mc.player.containerMenu.slots);
        List<Slot> slotsToThrow = info.slotsToThrow();
        List<Slot> slotsToSteal = new ArrayList<>(mc.player.containerMenu.slots.stream()
                .filter(slot -> slot.container != mc.player.getInventory() && slot.hasItem() && !slotsToThrow.contains(slot))
                .toList());

        boolean chestEmpty = slotsToSteal.isEmpty();
        int delayMs = delay.getProperty().intValue();

        if (!chestEmpty && !inventoryFull) {
            if (System.currentTimeMillis() - lastStealTime >= delayMs) {
                lastStealTime = System.currentTimeMillis();
                Collections.shuffle(slotsToSteal);
                Slot slot = slotsToSteal.remove(0);
                mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slot.index, 0, ClickType.QUICK_MOVE, mc.player);
            }
        } else if ((chestEmpty || inventoryFull) && autoClose.getProperty()) {
            if (System.currentTimeMillis() - lastStealTime >= delayMs) {
                lastStealTime = System.currentTimeMillis();
                mc.player.closeContainer();
            }
        }
    }
}