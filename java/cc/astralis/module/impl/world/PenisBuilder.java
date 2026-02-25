package cc.astralis.module.impl.world;

import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.player.scaffold.ScaffoldWalkUtil;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PenisBuilder extends Module {
    private final ArrayList<BlockPos> blockPosSequence = new ArrayList<>();
    private final NumberProperty placeDelay = new NumberProperty("Place Delay", 2, 1, 10, 1);
    private final BooleanProperty swing = new BooleanProperty("Swing Hand", true);
    private final BooleanProperty rotate = new BooleanProperty("Rotations", true);

    private int currentIndex = 0;
    private int tickCounter = 0;
    private boolean structureInitialized = false;
    private int currentBlockSlot = -1;
    private Direction direction;

    public PenisBuilder() {
        super(Category.WORLD);
        registerProperties(placeDelay, swing, rotate);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (!structureInitialized) {
            initializePenis();
            currentBlockSlot = ScaffoldWalkUtil.getBlockSlot(true).slot();
            return;
        }

        if (currentIndex >= blockPosSequence.size()) {
            toggle();
            return;
        }

        if (tickCounter >= placeDelay.getProperty().intValue()) {
            placeCurrentBlock();
            tickCounter = 0;
        } else {
            tickCounter++;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (rotate.getProperty() && structureInitialized && currentIndex < blockPosSequence.size()) {
            BlockPos pos = blockPosSequence.get(currentIndex);
            float[] rotations = getRotationsToBlock(pos);
            RotationComponent.setRotations(rotations, 180, 180);
        }
    }

    private void initializePenis() {
        for (Direction direction : Direction.values()) {
         //   this.direction = direction;
            BlockPos origin = mc.player.blockPosition().below().relative(direction);

            blockPosSequence.clear();

            blockPosSequence.add(origin);
            blockPosSequence.add(origin.offset(1, 0, 0));
            blockPosSequence.add(origin.offset(2, 0, 0));

            blockPosSequence.add(origin.offset(0, 1, 0));
            blockPosSequence.add(origin.offset(0, 2, 0));


            structureInitialized = true;
        }
    }

    private void placeCurrentBlock() {
        BlockPos pos = blockPosSequence.get(currentIndex);

        if (mc.player.getInventory().getSelectedSlot() != currentBlockSlot) {
            mc.player.getInventory().setSelectedSlot(currentBlockSlot);
            return;
        }

        if (rotate.getProperty()) {
            float[] rotations = getRotationsToBlock(pos);
            RotationComponent.setRotations(rotations, 180, 180);
        }

        Vec3 hitVec = new Vec3(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5
        );

        for (Direction direction : Direction.values()) {
            if (direction == this.direction)
                continue;
            mc.gameMode.useItemOn(
                    mc.player,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(hitVec, direction.getOpposite(), pos, false)
            );

            this.direction = direction;
        }

        if (swing.getProperty()) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }

        currentIndex++;
    }

    private float[] getRotationsToBlock(BlockPos pos) {
        double deltaX = pos.getX() + 0.5 - mc.player.getX();
        double deltaY = pos.getY() + 0.5 - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double deltaZ = pos.getZ() + 0.5 - mc.player.getZ();

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));

        return new float[]{yaw, pitch};
    }

    @Override
    public void onEnable() {
        currentIndex = 0;
        tickCounter = 0;
        structureInitialized = false;
        currentBlockSlot = -1;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        currentIndex = 0;
        tickCounter = 0;
        structureInitialized = false;
        currentBlockSlot = -1;
        super.onDisable();
    }
}