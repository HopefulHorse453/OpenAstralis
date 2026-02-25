package cc.astralis.module.impl.movement.speed;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.PlayerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WatchdogSpeed2 extends SubModule {

    public WatchdogSpeed2(Module parentClass) {
        super(parentClass,"Watchdog 2");
    }

    private boolean up = false;

    @Override
    public void onEnable() {
        up = false;
        super.onEnable();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        double motion = mc.player.getDeltaMovement().y;
        motion += switch (offGroundTicks) {
            case 1 -> 0.05;
            case 2 -> 0.012;
            case 3 -> -0.135;
            case 4 -> -0.2;
            default -> 0;
        };

        PlayerUtil.setMotionY(motion);

        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
            MoveUtil.strafe(MoveUtil.getSpeed());
            up = false;
        } else {
            if (offGroundTicks == 1) {
                double speedWithSpeed = MoveUtil.getPerfectValue(0.33f, 0.4f, 0.48f);
                MoveUtil.strafe(Math.max(speedWithSpeed, MoveUtil.getSpeed()));
            }

            BlockPos blockBelow = BlockPos.containing(mc.player.getX(), mc.player.getY() + mc.player.getDeltaMovement().y, mc.player.getZ());
            BlockState state = mc.level.getBlockState(blockBelow);
            VoxelShape shape = state.getCollisionShape(mc.level, blockBelow);

            if (!shape.isEmpty() && offGroundTicks > 2) {
                MoveUtil.strafe(MoveUtil.getSpeed());
            }

            if (offGroundTicks >= 2 && (
                    isFullBlock(
                            mc.player.getX(),
                            mc.player.getY() + mc.player.getDeltaMovement().y * 3,
                            mc.player.getZ()
                    ) || offGroundTicks == 9
            )) {
                double distanceToGround = getDistanceToGround();


                if (!up) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(0, 0.075, 0));
                    up = true;
                } else {
                    up = false;
                }
                MoveUtil.strafe();
            }
        }
    }

    private boolean isFullBlock(double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockState state = mc.level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(mc.level, pos);

        return shape == Shapes.block();
    }

    public static double getDistanceToGround() {
        double playerY = mc.player.getY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (double y = playerY; y >= 0; y -= 0.5) {
            pos.set(mc.player.getX(), y, mc.player.getZ());
            BlockState state = mc.level.getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(mc.level, pos);

            if (!shape.isEmpty()) {
                return playerY - (y + shape.max(Direction.Axis.Y));
            }
        }
        return -1;
    }
}
