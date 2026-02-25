package cc.astralis.module.impl.player;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.util.player.PlayerUtil;
import java.util.function.Predicate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoToolModule extends Module {
    private int originalSlot = -1;

    private final BooleanProperty counterShieldWithAxe = new BooleanProperty("Counter Shield with Axe", true);
    private final BooleanProperty onlyWhileSneaking = new BooleanProperty("Only While Sneaking", false);
    private final BooleanProperty switchBackEnabled = new BooleanProperty("Switch Back", true);

    public AutoToolModule() {
        super(Category.PLAYER);
        registerProperties(counterShieldWithAxe, onlyWhileSneaking, switchBackEnabled);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.hitResult instanceof BlockHitResult blockHitResult) {
            if (mc.level.getBlockState(blockHitResult.getBlockPos()).isAir() || onlyWhileSneaking.getProperty() && !mc.player.isShiftKeyDown() )
                return;

            if (mc.options.keyAttack.isDown()) {
                if (originalSlot == -1) {
                    originalSlot = mc.player.getInventory().getSelectedSlot();
                    PlayerUtil.doAutoTool(blockHitResult.getBlockPos());
                }
            } else if (originalSlot != -1 && switchBackEnabled.getProperty()) {
                switchBack();
            }
        } else if (originalSlot != -1 && !mc.options.keyAttack.isDown() && switchBackEnabled.getProperty()) {
            switchBack();
        }

        if (counterShieldWithAxe.getProperty() && mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
            if (mc.hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof Player target) {
                if (target.isUsingItem() && target.getUseItem().is(Items.SHIELD)) {
                    int axeSlot = getHotbarSlot(item -> item.getItem() instanceof AxeItem);
                    if (axeSlot != -1 && mc.player.getInventory().getSelectedSlot() != axeSlot) {
                        originalSlot = mc.player.getInventory().getSelectedSlot();
                        mc.player.getInventory().setSelectedSlot(axeSlot);
                    }
                }
            }
        }
    }

    private static int getHotbarSlot(Predicate<ItemStack> predicate) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (predicate.test(stack)) return i;
        }
        return -1;
    }

    private void switchBack() {
        mc.player.getInventory().setSelectedSlot(originalSlot);
        originalSlot = -1;
    }
}
