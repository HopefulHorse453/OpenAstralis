package cc.astralis.module.impl.player;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.player.InventoryUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;

public class InventoryManagerModule extends Module {
    private final NumberProperty actionDelay = new NumberProperty("Action Delay", 3, 1, 20, 1);
    private final NumberProperty randomDelay = new NumberProperty("Random Delay", 2, 0, 10, 1);
    private final BooleanProperty shuffleActions = new BooleanProperty("Shuffle Actions", true);

    private final NumberProperty swordSlot = new NumberProperty("Sword Slot", 0, 0, 8, 1);
    private final NumberProperty blockSlot = new NumberProperty("Block Slot", 2, 0, 8, 1);
    private final NumberProperty gappleSlot = new NumberProperty("Gapple Slot", 1, 0, 8, 1);
    private final NumberProperty firstToolSlot = new NumberProperty("First Tool Slot", 3, 0, 8, 1);
    private final BooleanProperty throwJunk = new BooleanProperty("Throw Junk", true);
    private final BooleanProperty openInvOnly = new BooleanProperty("Open Inventory Only", true);

    private int delayTicks = 0;
    private int currentDelay = 0;
    private final Random random = new Random();

    public InventoryManagerModule() {
        super(Category.PLAYER);
        registerProperties(actionDelay, randomDelay, shuffleActions,
                swordSlot, blockSlot, gappleSlot, firstToolSlot, throwJunk, openInvOnly);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.gameMode == null || mc.level == null) {
            return;
        }

        if (openInvOnly.getProperty() && !(mc.screen instanceof InventoryScreen)) {
            delayTicks = 0;
            return;
        }

        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        currentDelay = actionDelay.getProperty().intValue() +
                random.nextInt(randomDelay.getProperty().intValue() + 1);
        InventoryUtil.Info info = InventoryUtil.sort(mc.player.containerMenu.slots);
        List<Runnable> possibleActions = new ArrayList<>();

        for (int i = 0; i < info.slotsArmor().length; i++) {
            Slot armorSlot = info.slotsArmor()[i];
            if (armorSlot != null && armorSlot.index > 8) {
                possibleActions.add(() -> {
                    InventoryUtil.click(armorSlot.index, 0, ClickType.QUICK_MOVE);
                });
            }
        }

        Slot[] slotSword = info.slotWeapon();
        int targetSwordSlot = 36 + swordSlot.getProperty().intValue();
        if (slotSword[0] != null && slotSword[0].index != targetSwordSlot) {
            final Slot swordToMove = slotSword[0];
            final int swapButton = swordSlot.getProperty().intValue();
            possibleActions.add(() -> {
                InventoryUtil.click(swordToMove.index, swapButton, ClickType.SWAP);
            });
        }

        Slot blockSlotItem = InventoryUtil.findBestBlocks();
        int targetBlockSlot = 36 + blockSlot.getProperty().intValue();
        if (blockSlotItem != null && blockSlotItem.index != targetBlockSlot) {
            Slot targetSlot = mc.player.containerMenu.slots.get(targetBlockSlot);
            boolean shouldSwap = !targetSlot.hasItem() ||
                    !(targetSlot.getItem().getItem() instanceof BlockItem) ||
                    targetSlot.getItem().getItem() != blockSlotItem.getItem().getItem();

            if (shouldSwap) {
                final Slot blockToMove = blockSlotItem;
                final int swapButton = blockSlot.getProperty().intValue();
                possibleActions.add(() -> {
                    InventoryUtil.click(blockToMove.index, swapButton, ClickType.SWAP);
                });
            }
        }

        Slot gappleSlotItem = InventoryUtil.findGapples();
        int targetGappleSlot = 36 + gappleSlot.getProperty().intValue();
        if (gappleSlotItem != null && gappleSlotItem.index != targetGappleSlot) {
            Slot targetSlot = mc.player.containerMenu.slots.get(targetGappleSlot);
            boolean shouldSwap = !targetSlot.hasItem() ||
                    (targetSlot.getItem().getItem() != Items.GOLDEN_APPLE &&
                            targetSlot.getItem().getItem() != Items.ENCHANTED_GOLDEN_APPLE);

            if (shouldSwap) {
                final Slot gappleToMove = gappleSlotItem;
                final int swapButton = gappleSlot.getProperty().intValue();
                possibleActions.add(() -> {
                    InventoryUtil.click(gappleToMove.index, swapButton, ClickType.SWAP);
                });
            }
        }

        Slot[] slotsTool = info.slotsTool();
        int availableToolSlots = Math.min(3, 9 - firstToolSlot.getProperty().intValue());
        for (int i = 0; i < Math.min(availableToolSlots, slotsTool.length); i++) {
            Slot slotTool = slotsTool[i];
            int targetToolSlot = 36 + firstToolSlot.getProperty().intValue() + i;
            if (slotTool != null && slotTool.index != targetToolSlot) {
                final Slot toolToMove = slotTool;
                final int swapButton = firstToolSlot.getProperty().intValue() + i;
                possibleActions.add(() -> {
                    InventoryUtil.click(toolToMove.index, swapButton, ClickType.SWAP);
                });
            }
        }

        if (throwJunk.getProperty()) {
            List<Slot> slotsToThrow = new ArrayList<>(info.slotsToThrow());
            if (!slotsToThrow.isEmpty()) {
                Slot itemToThrow = slotsToThrow.get(0);
                possibleActions.add(() -> {
                    InventoryUtil.click(itemToThrow.index, 1, ClickType.THROW);
                });
            }
        }

        if (shuffleActions.getProperty() && possibleActions.size() > 1) {
            Collections.shuffle(possibleActions);
        }

        if (!possibleActions.isEmpty()) {
            possibleActions.get(0).run();
            delayTicks = currentDelay;
        }
    }

    @Override
    public void onDisable() {
        delayTicks = 0;
        currentDelay = 0;
        super.onDisable();
    }
}