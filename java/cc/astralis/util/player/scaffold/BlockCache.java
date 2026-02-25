package cc.astralis.util.player.scaffold;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * @author Kawase
 * @since 26.10.2025
 */
public class BlockCache {
    public final BlockPos blockPos;
    public final Direction direction;

    public BlockCache(BlockPos blockPos, Direction direction) {
        this.blockPos = blockPos;
        this.direction = direction;
    }
}
