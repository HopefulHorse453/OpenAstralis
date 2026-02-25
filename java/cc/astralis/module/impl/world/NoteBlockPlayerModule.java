package cc.astralis.module.impl.world;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;

public class NoteBlockPlayerModule extends Module {
    public NoteBlockPlayerModule() {
        super(Category.WORLD);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        BlockPos playerPos = mc.player.blockPosition();
        int radius = 5;

        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-radius, -radius, -radius), playerPos.offset(radius, radius, radius))) {
            BlockState blockState = mc.level.getBlockState(pos);
            if (blockState.getBlock() == Blocks.NOTE_BLOCK) {
                int note = blockState.getValue(NoteBlock.NOTE);
                ChatUtil.printDebug("Note block at " + pos.toShortString() + " has note value: " + note);
            }
        }
    }
}
