package cc.astralis.util.player;

import cc.astralis.interfaces.IAccess;
import cc.astralis.module.impl.combat.AntiBotModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerUtil implements IAccess {
    public static void setMotionY(double y) {
        mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, y, mc.player.getDeltaMovement().z);
    }

    public static boolean isEntityWithinRange(LivingEntity target, float range) {
        return mc.player.distanceTo(target) <= range
                || RotationUtil.getDistanceToEntityBox(target) <= range ||
                mc.player.canInteractWithEntity(target, range - 3 - 0.001) ||
                mc.hitResult.getType() == HitResult.Type.ENTITY;
    }

    public static double getMotionY() {
        return mc.player.getDeltaMovement().y;
    }

    public static void doAutoTool(BlockPos pos) {
        if (mc.player == null || mc.level == null) return;
        int bestToolSlot = 0;
        BlockState blockState = mc.level.getBlockState(pos);

        for (int i = 0; i < 9; ++i) {
            if (mc.player.getInventory().getItem(i).getDestroySpeed(blockState) >
                    mc.player.getInventory().getItem(bestToolSlot).getDestroySpeed(blockState)) {
                bestToolSlot = i;
            }
        }

        if (bestToolSlot != mc.player.getInventory().getSelectedSlot()) {
            mc.player.getInventory().setSelectedSlot(bestToolSlot);
        }
    }

    public static float getDistanceToGround() {
        for (int i = (int) (mc.player.getY()); i > -1; i--) {
            final BlockPos pos = new BlockPos((int) mc.player.getX(), i, (int) mc.player.getZ());
            String translationKey = mc.level.getBlockState(pos).getBlock().getDescriptionId();

            if (translationKey.contains("short_grass") || translationKey.contains("tall_grass"))
                continue;

            if (!(mc.level.getBlockState(pos).getBlock() instanceof AirBlock)) {
                return (float) (mc.player.getY() - pos.getY()) - 1;
            }
        }
        return 0;
    }

    public static void placeBlockAtCrossHair() {
        BlockPos vecPos = ((BlockHitResult) mc.hitResult).getBlockPos().relative(((BlockHitResult) mc.hitResult).getDirection());

        mc.gameMode.useItemOn(
                mc.player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        new Vec3(vecPos.getX(), vecPos.getY(), vecPos.getZ()),
                        ((BlockHitResult) mc.hitResult).getDirection(),
                        ((BlockHitResult) mc.hitResult).getBlockPos(),
                        false
                )
        );
    }

    public static boolean isHoldingFood() {
        FoodProperties foodComponent = mc.player.getInventory().getSelectedItem().get(DataComponents.FOOD);
        return foodComponent != null;
    }

    public static boolean isHoldingWeapon() {
        ItemStack stack = mc.player.getInventory().getSelectedItem();
        Item item = stack.getItem();
        return mc.player.getUseItem().getUseAnimation() == ItemUseAnimation.BLOCK || mc.player.getInventory().getSelectedItem().is(ItemTags.SWORDS)|| item instanceof AxeItem || item instanceof MaceItem || item instanceof TridentItem;
    }

    public static boolean isPlayerOverVoid() {
        BlockPos playerPos = mc.player.blockPosition();

        for (int y = playerPos.getY() - 1; y >= playerPos.getY() - 10; y--) {
            BlockPos checkPos = new BlockPos(playerPos.getX(), y, playerPos.getZ());

            if (!mc.level.getBlockState(checkPos).isAir()) {
                return false;
            }
        }

        return true;
    }

    public static boolean isHoldingBlocks() {
        ItemStack stack = mc.player.getInventory().getSelectedItem();
        Item item = stack.getItem();
        return item instanceof BlockItem;
    }

    public static float getBreakTicks(BlockPos bp, ItemStack tool) {
        ItemStack oldHeld = mc.player.getMainHandItem();
        mc.player.getInventory().getNonEquipmentItems().set(mc.player.getInventory().getSelectedSlot(), tool);
        BlockState bs = mc.level.getBlockState(bp);
        float ticks = (float) Math.ceil(1f / bs.getDestroyProgress(mc.player, mc.level, bp));
        mc.player.getInventory().getNonEquipmentItems().set(mc.player.getInventory().getSelectedSlot(), oldHeld);
        return ticks;
    }

    public static List<LivingEntity> getTargets(boolean animals, boolean monsters, boolean ignoreTeammates, boolean invisible, boolean throughWalls, float range) {
        return mc.level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(mc.player.blockPosition()).inflate(range),
                        entity -> {
                            if (entity == null || entity.isDeadOrDying() || !entity.isAlive() || entity.deathTime > 0) {
                                return false;
                            }

                            if (entity == mc.player) {
                                return false;
                            }

                            if (entity instanceof ArmorStand) {
                                return false;
                            }

                            if (entity instanceof Bat) {
                                return false;
                            }

                            if (entity instanceof Animal || entity instanceof Villager) {
                                return animals;
                            }

                            if (entity instanceof Mob || entity instanceof AbstractGolem) {
                                return monsters;
                            }

                            if (entity instanceof Player) {
                                if (entity.isInvisible() && !invisible) {
                                    return false;
                                }

                                if (TeamUtil.isSameTeam(entity) && ignoreTeammates) {
                                    return false;
                                }

                                if (AntiBotModule.isBot((Player) entity)) {
                                    return false;
                                }

                                if (entity.getScoreboardName().equals(" ")) {
                                    return false;
                                }
                            }

                            if (!mc.player.hasLineOfSight(entity) && !throughWalls) {
                                return false;
                            }

                            return true;
                        }).stream()
                .sorted(Comparator.comparingDouble(entity -> mc.player.distanceTo(entity)))
                .collect(Collectors.toList());
    }
}
