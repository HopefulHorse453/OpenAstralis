package cc.astralis.util.player.scaffold;

import cc.astralis.interfaces.IAccess;
import cc.astralis.module.impl.movement.ScaffoldWalkModule.BlockData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static cc.astralis.module.impl.movement.ScaffoldWalkModule.BlockData;

public class ScaffoldWalkUtil implements IAccess {
    public record BlockSlot(int slot, InteractionHand hand) { /* w */ }

    public static BlockSlot[] getBlockSlots(boolean ignoreOffhand) {
        List<BlockSlot> blockSlots = new ArrayList<>();

        if (!ignoreOffhand) {
            ItemStack offhandStack = mc.player.getInventory().getItem(40);
            if (offhandStack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (canPlaceBlock(block)) {
                    blockSlots.add(new BlockSlot(0, InteractionHand.OFF_HAND));
                }
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.player.getInventory().getNonEquipmentItems().get(i);
            if (itemStack.getItem() instanceof BlockItem blockItem && itemStack.getCount() > 0) {
                Block block = blockItem.getBlock();
                if (canPlaceBlock(block)) {
                    blockSlots.add(new BlockSlot(i, InteractionHand.MAIN_HAND));
                }
            }
        }

        return blockSlots.toArray(new BlockSlot[0]);
    }

    public static int getStackCount(BlockSlot slot) {
        if (slot.hand() == InteractionHand.OFF_HAND) {
            return mc.player.getInventory().getItem(40).getCount();
        } else {
            return mc.player.getInventory().getNonEquipmentItems().get(slot.slot()).getCount();
        }
    }

    public static BlockSlot getBlockSlot(boolean ignore) {
        BlockSlot[] slots = getBlockSlots(ignore);
        return slots.length > 0 ? slots[0] : new BlockSlot(-1, InteractionHand.MAIN_HAND);
    }

    public static BlockState getBlockStateRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        BlockPos playerPos = mc.player.blockPosition();
        return mc.level.getBlockState(playerPos.offset((int) offsetX, (int) offsetY, (int) offsetZ));
    }

    public static boolean isAirBlock(BlockPos pos) {
        return mc.level.getBlockState(pos).getBlock() instanceof AirBlock;
    }

    public static BlockData getBlockData(final int baseY) {
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
                                    if (canPlaceBlock(state.getBlock())) {
                                        return new BlockData(finalBlockPos, direction.getOpposite());
                                    }
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
}