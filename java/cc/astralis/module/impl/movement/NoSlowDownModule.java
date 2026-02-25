package cc.astralis.module.impl.movement;

import cc.astralis.Astralis;
import cc.astralis.component.impl.network.BlinkComponent;
import cc.astralis.component.impl.ui.ProgressBarComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.event.events.impl.game.SlowDownEvent;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.network.PacketUtil;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class NoSlowDownModule extends Module {
    private final NumberProperty swordSlowDown = new NumberProperty("Sword Slow Down", 1, 0, 1, 0.1f),
            foodSlowDown = new NumberProperty("Food Slow Down", 1, 0, 1, 0.1f),
            potionSlowDown = new NumberProperty("Potion Slow Down", 1, 0, 1, 0.1f),
            bowSlowDown = new NumberProperty("Bow Slow Down", 1, 0, 1, 0.1f);
    private final ModeProperty swordMethod = new ModeProperty("Sword Method", "Vanilla", "Vanilla", "Watchdog", "Old Watchdog", "No Cheat Plus", "Old Intave", "Intave"),
            foodMethod = new ModeProperty("Food Method", "Vanilla", "Vanilla", "Watchdog", "Old Watchdog", "No Cheat Plus", "Old Intave", "Intave"),
            potionMethod = new ModeProperty("Potion Method", "Vanilla", "Vanilla", "Watchdog", "Old Watchdog", "No Cheat Plus", "Old Intave"),
            bowMethod = new ModeProperty("Bow Method", "Vanilla", "Vanilla", "Watchdog", "Old Watchdog", "No Cheat Plus", "Old Intave", "Intave");

    public ItemInfo itemInfo;
    private int tick;
    private boolean hasUnblinked = false;

    public NoSlowDownModule() {
        super(Category.MOVEMENT);
        registerProperties(swordSlowDown,
                foodSlowDown, potionSlowDown, bowSlowDown,
                swordMethod, foodMethod, potionMethod, bowMethod
        );
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        BlinkComponent blinkComponent = Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class);

        if (!mc.player.isUsingItem()) {
            itemInfo = new ItemInfo("none", 1f, ItemUseAnimation.NONE);
            tick = 0;
            if (!hasUnblinked) {
                blinkComponent.stopBlinking();
                hasUnblinked = true;
            }

            ProgressBarComponent.removeBar("use");
            return;
        }

        if (mc.player.isUsingItem() && mc.player.getUseItemRemainingTicks() > 0) {
            tick++;
        } else {
            tick = 0;
        }

        switch (mc.player.getUseItem().getUseAnimation()) {
            case BLOCK -> itemInfo = new ItemInfo(swordMethod.getProperty(), swordSlowDown.getProperty().floatValue(), ItemUseAnimation.BLOCK);
            case BOW -> itemInfo = new ItemInfo(bowMethod.getProperty(), bowSlowDown.getProperty().floatValue(), ItemUseAnimation.BOW);
            case EAT -> itemInfo = new ItemInfo(foodMethod.getProperty(), foodSlowDown.getProperty().floatValue(), ItemUseAnimation.EAT);
            case DRINK -> itemInfo = new ItemInfo(potionMethod.getProperty(), potionSlowDown.getProperty().floatValue(), ItemUseAnimation.DRINK);
        }

        switch (itemInfo.method()) {
            case "Watchdog":
                if (itemInfo.useAction == ItemUseAnimation.EAT) {
                    if (tick > 12) {
                        if (!blinkComponent.isBlinking()) {
                            blinkComponent.startBlinking();
                            ProgressBarComponent.createBar("use");
                            hasUnblinked = false;
                        } else {
                            ProgressBarComponent.updateBar("use", tick / 30f);
                        }
                    }

                    if (!hasUnblinked && mc.player.getUseItemRemainingTicks() < 0 && blinkComponent.isBlinking()) {
                        blinkComponent.stopBlinking();
                        ProgressBarComponent.removeBar("use");
                        hasUnblinked = true;
                    }
                }
                break;

            case "Old Watchdog":
                PacketUtil.sendSequenced(sequence ->
                        new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND,
                                new BlockHitResult(Vec3.ZERO, Direction.UP,
                                        new BlockPos(BlockPos.ZERO.getX(), BlockPos.ZERO.getY(), BlockPos.ZERO.getZ()), false),
                                sequence));
                break;

            case "Intave":
                PacketUtil.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                        BlockPos.ZERO, Direction.UP));
                break;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getEventMode() == EventModes.POST || !itemInfo.method().equals("Watchdog")) return;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
    }

    @EventTarget
    public void onSlowDown(SlowDownEvent event) {
        if (Objects.equals(itemInfo.method(), "Watchdog")) {
            if (itemInfo.useAction == ItemUseAnimation.BLOCK && tick >= 1) {
                event.setCancelled(true);
                event.setSlowDown(itemInfo.slowDown());
            } else if (itemInfo.useAction == ItemUseAnimation.EAT && tick > 12) {
                event.setCancelled(true);
                event.setSlowDown(itemInfo.slowDown());
            }
        }
    }

    public record ItemInfo(String method, float slowDown, ItemUseAnimation useAction) { /* w */
    }
}