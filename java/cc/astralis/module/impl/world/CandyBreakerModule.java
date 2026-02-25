package cc.astralis.module.impl.world;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.network.PacketUtil;
import cc.astralis.util.player.PlayerUtil;
import cc.astralis.util.render.Render3DUtil;
import java.awt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class CandyBreakerModule extends Module {
    private final NumberProperty range = new NumberProperty("Range", 6, 1, 12, 1);

    private BlockPos currentBlock;
    private boolean rotate;
    private int breakProgress;

    public CandyBreakerModule() {
        super(Category.WORLD);
        registerProperty(range);
    }


    @EventTarget
    public void onUpdate(UpdateEvent event) {
        double range = this.range.getProperty().doubleValue();

        // this looks weird ass ngl~
        final int minX = (int) (mc.player.position().x - range),
                maxX = (int) (mc.player.position().x + range);

        final int minY = (int) (mc.player.position().y + mc.player.getEyeHeight() - range),
                maxY = (int) (mc.player.position().y + mc.player.getEyeHeight() + range);

        final int minZ = (int) (mc.player.position().z - range),
                maxZ = (int) (mc.player.position().z + range);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    final BlockState currentBlockState = mc.level.getBlockState(new BlockPos(x, y, z));

                    if (currentBlockState.getBlock() instanceof SkullBlock skullBlock &&
                            skullBlock.getType() == SkullBlock.Types.PLAYER) {
                        currentBlock = new BlockPos(x, y, z);

                        if (breakProgress == 0) {
                            rotate = true;
                            mc.player.swing(InteractionHand.MAIN_HAND);

                            PacketUtil.send(new ServerboundPlayerActionPacket(
                                    ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                                    currentBlock,
                                    Direction.DOWN
                            ));

                            rotate = false;
                        } else if (breakProgress >= PlayerUtil.getBreakTicks(currentBlock, mc.player.getMainHandItem())) {
                            rotate = true;
                            mc.player.swing(InteractionHand.MAIN_HAND);

                            PacketUtil.send(new ServerboundPlayerActionPacket(
                                    ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                                    currentBlock,
                                    Direction.DOWN
                            ));

                            mc.getTutorial().onDestroyBlock(mc.level, currentBlock, currentBlockState, 1.0F);
                            reset();
                            return;
                        } else {
                            mc.player.swing(InteractionHand.MAIN_HAND);
                        }

                        breakProgress++;
                        final int currentProgress = (int) (
                                ((double) breakProgress / PlayerUtil.getBreakTicks(currentBlock, mc.player.getMainHandItem())) * 100);

                        mc.level.destroyBlockProgress(mc.player.getId(), currentBlock, currentProgress / 10);
                    }
                }
            }
        }
    }

    private void reset() {
        if (currentBlock != null) {
            mc.level.destroyBlockProgress(mc.player.getId(), currentBlock, -1);
            PacketUtil.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, currentBlock, Direction.DOWN));
        }

        breakProgress = 0;
        currentBlock = null;

        rotate = false;
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        final int range = 32;
        for (BlockPos blockPos : BlockPos.betweenClosed(mc.player.blockPosition().offset(-range, -range, -range),
                mc.player.blockPosition().offset(range, range, range))) {
            BlockState blockState = mc.level.getBlockState(blockPos);
            Block block = blockState.getBlock();

            if (block instanceof SkullBlock skullBlock) {
                Render3DUtil.drawBoxESP(event.getMatricies(), new AABB(blockPos), currentBlock != null && mc.level.getBlockState(currentBlock) == blockState ? Color.red : Color.GREEN,150);
            }
        }
    }
}
