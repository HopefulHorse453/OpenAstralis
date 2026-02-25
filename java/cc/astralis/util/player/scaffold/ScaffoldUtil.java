package cc.astralis.util.player.scaffold;

import cc.astralis.interfaces.IAccess;
import cc.astralis.module.impl.movement.ScaffoldWalkModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Kawase
 * @since 26.10.2025
 */
public class ScaffoldUtil implements IAccess {
    public static int getTotalBlocksInInventory() {
        int total = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (ScaffoldWalkUtil.canPlaceBlock(block)) {
                    total += stack.getCount();
                }
            }
        }

        return total;
    }

    public static Vec3 getVec(BlockCache blockCache) {
        if (blockCache == null)
            return null;

        BlockPos pos = blockCache.blockPos;

        double x = pos.getX() + Math.random();
        double y = pos.getY() + Math.random();
        double z = pos.getZ() + Math.random();

        final HitResult movingObjectPosition = mc.hitResult;

        // Fallback vector.
        switch (blockCache.direction) {
            case DOWN -> y = pos.getY();
            case UP -> y = pos.getY() + 1;
            case NORTH -> z = pos.getZ();
            case SOUTH -> z = pos.getZ() + 1;
            case WEST -> x = pos.getX();
            case EAST -> x = pos.getX() + 1;
        }

        if (movingObjectPosition instanceof BlockHitResult blockHitResult) {
            if (blockHitResult.getBlockPos().equals(blockCache.blockPos) &&
                    blockHitResult.getDirection() == blockCache.direction) {
                x = blockHitResult.getLocation().x;
                y = blockHitResult.getLocation().y;
                z = blockHitResult.getLocation().z;
            }
        }

        return new Vec3(x, y, z);
    }


    public static boolean isAirBlock(BlockPos pos) {
        return mc.level.getBlockState(pos).getBlock() instanceof AirBlock;
    }

    public static BlockCache getBlockData(final int baseY) {
        BlockPos belowBlockPos = new BlockPos(mc.player.getBlockX(),
                baseY, mc.player.getBlockZ()
        ).below();

        if (isAirBlock(belowBlockPos)) {
            int[] offsets = { 0, 1, 2, 3 }, multipliers = { 1, -1 };

            for (int x : offsets) {
                for (int y : offsets) {
                    for (int z : offsets) {
                        for (int m : multipliers) {
                            final BlockPos blockPos = belowBlockPos.offset(x * m, y * m, z * m);

                            if (isAirBlock(blockPos)) {
                                for (Direction direction : Direction.values()) {
                                    final BlockPos finalBlockPos = blockPos.relative(direction);
                                    BlockState state = mc.level.getBlockState(finalBlockPos);
                                    if (canPlaceBlock(state.getBlock()))
                                        return new BlockCache(finalBlockPos, direction.getOpposite());
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public static boolean canPlaceBlock(Block block) {
        if (block == null || block == Blocks.AIR) return false;

        final BlockState material = block.defaultBlockState();

        if (block.getDescriptionId().contains("button"))
            return false;

        if (block == Blocks.TNT ||
                block == Blocks.NOTE_BLOCK ||
                block == Blocks.BEACON ||
                block == Blocks.COMMAND_BLOCK ||
                block == Blocks.REPEATING_COMMAND_BLOCK ||
                block == Blocks.CHAIN_COMMAND_BLOCK ||
                block == Blocks.STRUCTURE_BLOCK ||
                block == Blocks.JIGSAW ||
                block == Blocks.BARRIER ||
                block == Blocks.LIGHT ||
                block == Blocks.SPAWNER) {
            return false;
        }

        if (block == Blocks.SAND ||
                block == Blocks.GRAVEL ||
                block == Blocks.RED_SAND ||
                block == Blocks.ANVIL ||
                block == Blocks.CHIPPED_ANVIL ||
                block == Blocks.DAMAGED_ANVIL ||
                block == Blocks.DRAGON_EGG ||
                block == Blocks.SCAFFOLDING ||
                block == Blocks.SHORT_GRASS ||
                block == Blocks.TALL_GRASS ||
                block == Blocks.COBWEB ||
                block == Blocks.POINTED_DRIPSTONE) {
            return false;
        }

        if (block == Blocks.FURNACE ||
                block == Blocks.BLAST_FURNACE ||
                block == Blocks.SMOKER ||
                block == Blocks.CRAFTING_TABLE ||
                block == Blocks.CRAFTER ||
                block == Blocks.DISPENSER ||
                block == Blocks.DROPPER ||
                block == Blocks.HOPPER ||
                block == Blocks.BREWING_STAND ||
                block == Blocks.ENCHANTING_TABLE ||
                block == Blocks.ENDER_CHEST ||
                block == Blocks.CHEST ||
                block == Blocks.RESPAWN_ANCHOR ||
                block == Blocks.LODESTONE) {
            return false;
        }

        if (block == Blocks.PISTON ||
                block == Blocks.STICKY_PISTON ||
                block == Blocks.OBSERVER ||
                block == Blocks.TARGET ||
                block == Blocks.TRIPWIRE_HOOK ||
                block == Blocks.LEVER ||
                block == Blocks.REPEATER ||
                block == Blocks.COMPARATOR ||
                block == Blocks.DAYLIGHT_DETECTOR) {
            return false;
        }

        return material.isSolid();
    }

    public static int findBiggestBlockStack() {
        int biggestStackSlot = -1;
        int biggestStackSize = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem) {
                if (stack.getCount() > biggestStackSize) {
                    biggestStackSize = stack.getCount();
                    biggestStackSlot = i;
                }
            }
        }
        return biggestStackSlot;
    }
}
