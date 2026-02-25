package cc.astralis.module.impl.combat;

import astralis.mixin.accessor.player.ClientPlayerInteractionManagerAccessor;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.EntityInteractEvent;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.util.player.InventoryUtil;
import cc.astralis.util.player.ToolType;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.enchantment.Enchantments;

public class MaceSwapModule extends Module {
    private final BooleanProperty windBurst = new BooleanProperty("Wind Burst", true);
    private final BooleanProperty breach = new BooleanProperty("Breach", true);
    private final BooleanProperty onlySword = new BooleanProperty("Only Sword", false);

    private int originalSlot = -1;

    public MaceSwapModule() {
        super(Category.COMBAT);
        registerProperties(windBurst, breach, onlySword);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.screen != null || event.getEventMode() == EventModes.POST ) return;
        if (originalSlot != -1) {
            mc.player.getInventory().setSelectedSlot(originalSlot);
            ((ClientPlayerInteractionManagerAccessor) mc.gameMode).syncSlot();
            originalSlot = -1;
        }
    }

    @EventTarget
    public void onAttack(EntityInteractEvent event) {
        if (mc.player == null || originalSlot != -1) return;

        if (onlySword.getProperty()) {
            ItemStack selected = mc.player.getInventory().getSelectedItem();
            if (InventoryUtil.getToolType(selected) != ToolType.Sword) return;
        }

        int targetSlot = -1;

        if (windBurst.getProperty() || breach.getProperty()) {
            targetSlot = getHotbarSlot(stack -> {
                if (stack.isEmpty()) return false;
                boolean isMace = stack.getItem() instanceof MaceItem;
                if (!isMace) return false;
                return windBurst.getProperty() && stack.getEnchantments().entrySet().stream().anyMatch(e -> e.getKey().is(Enchantments.WIND_BURST)) ||
                        breach.getProperty() && stack.getEnchantments().entrySet().stream().anyMatch(e -> e.getKey().is(Enchantments.BREACH));
            });
        }

        if (targetSlot == -1) {
            targetSlot = getHotbarSlot(stack -> !stack.isEmpty() && stack.getItem() instanceof MaceItem);
        }

        if (targetSlot == -1) {
            Predicate<ItemStack> weaponPred = onlySword.getProperty()
                    ? (stack) -> InventoryUtil.getToolType(stack) == ToolType.Sword
                    : (stack) -> {
                ToolType t = InventoryUtil.getToolType(stack);
                return t == ToolType.Sword || t == ToolType.Axe;
            };
            targetSlot = getHotbarSlot(weaponPred);
        }

        if (targetSlot != -1 && targetSlot != mc.player.getInventory().getSelectedSlot()) {
            originalSlot = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(targetSlot);
            ((ClientPlayerInteractionManagerAccessor) mc.gameMode).syncSlot();
        }
    }

    private static int getHotbarSlot(Predicate<ItemStack> predicate) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (predicate.test(stack)) return i;
        }
        return -1;
    }
}