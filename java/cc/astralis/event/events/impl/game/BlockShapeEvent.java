package cc.astralis.event.events.impl.game;

import cc.astralis.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

@Getter
@Setter
@AllArgsConstructor
public class BlockShapeEvent implements Event {
    public BlockState state;
    public BlockPos pos;
    public VoxelShape shape;
}
