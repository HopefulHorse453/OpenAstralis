package cc.astralis.module.impl.movement.flight;

import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public class BlockFlight extends SubModule {
    public BlockFlight(Module parentClass) {
        super(parentClass, "Block");
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        RotationComponent.setRotations(mc.player.getYRot() - 180, 81, 100, 100);

        if (mc.level.getBlockState(mc.player.blockPosition().below()).is(Blocks.AIR)) {
            mc.player.swing(InteractionHand.MAIN_HAND);
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(mc.hitResult.getLocation(), Direction.DOWN, mc.player.blockPosition().below(), false));
        }
    }
}
