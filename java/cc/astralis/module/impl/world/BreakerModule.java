package cc.astralis.module.impl.world;

import cc.astralis.component.impl.client.BedWhiteListComponent;
import cc.astralis.component.impl.ui.ProgressBarComponent;
import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.event.events.impl.game.WorldChangeEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.network.PacketUtil;
import cc.astralis.util.player.PlayerUtil;
import cc.astralis.util.player.RotationUtil;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BreakerModule extends Module {
    private final NumberProperty breakRadius = new NumberProperty("Break Range", 5.5f, 1f, 6f, 0.5f);
    private final BooleanProperty rotations = new BooleanProperty("Rotate", true);
    private final BooleanProperty breakBlockSurrounding = new BooleanProperty("Break Block surrounding", true);
    private final BooleanProperty keepRotation = new BooleanProperty("Keep Rotation", false);
    private final BooleanProperty whitelistOwn = new BooleanProperty("Whitelist Own", true);

    private BlockPos userBed;
    private boolean didFindPlayerBed;

    public boolean rotate = false;

    public static class BreakCache {
        BlockPos bedPos;
        BlockPos secondPos;
        public boolean isBreaking;
        boolean bedProgress;
        boolean secondProgress;
        int tick;
        int originalSlot;
        float maxTicks;

        BreakCache() {
            reset();
        }

        void reset() {
            bedPos = null;
            secondPos = null;
            isBreaking = false;
            bedProgress = false;
            secondProgress = false;
            tick = 0;
            originalSlot = -1;
            maxTicks = 0f; // reset
        }
    }

    public final BreakCache breakCache = new BreakCache();

    public BreakerModule() {
        super(Category.WORLD);
        registerProperties(breakRadius, rotations, breakBlockSurrounding, keepRotation, whitelistOwn);
    }

    @Override
    public void onEnable() {
        didFindPlayerBed = false;
        breakCache.reset();
        rotate = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.player != null && breakCache.isBreaking) {
            if (breakCache.bedPos != null) {
                mc.level.destroyBlockProgress(mc.player.getId(), breakCache.bedPos, -1);
                finalizeBlockBreak(breakCache.bedPos, breakCache.bedProgress);
            }
            if (breakCache.secondPos != null) {
                finalizeBlockBreak(breakCache.secondPos, breakCache.secondProgress);
            }
            if (breakCache.originalSlot != -1 && breakCache.originalSlot != mc.player.getInventory().getSelectedSlot()) {
                PacketUtil.sendNoEvent(new ServerboundSetCarriedItemPacket(breakCache.originalSlot));
                mc.player.getInventory().setSelectedSlot(breakCache.originalSlot);
            }
        }

        breakCache.reset();
        rotate = false;
        ProgressBarComponent.clear();
        super.onDisable();
    }

    @EventTarget
    public void onPacketSend(PacketEvent event) {
        if (breakCache.isBreaking && event.getPacket() instanceof ServerboundPlayerActionPacket) {
            ServerboundPlayerActionPacket packet = (ServerboundPlayerActionPacket) event.getPacket();
            if (packet.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK ||
                    packet.getAction() == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK ||
                    packet.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onWorldChangeEvent(WorldChangeEvent event) {
        didFindPlayerBed = false;
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getEventMode() == EventModes.POST || mc.player == null || mc.level == null) {
            return;
        }

        if (!didFindPlayerBed) {
            final int range = 20;
            for (BlockPos blockPos : BlockPos.betweenClosed(mc.player.blockPosition().offset(-range, -range, -range),
                    mc.player.blockPosition().offset(range, range, range))) {

                BlockState blockState = mc.level.getBlockState(blockPos);
                Block block = blockState.getBlock();

                if (block instanceof BedBlock) {
                    userBed = blockPos;
                    didFindPlayerBed = true;
                    ChatUtil.print("found bed");
                    break;
                }
            }
        }

        if (rotations.getProperty() && keepRotation.getProperty() && breakCache.isBreaking) {
            BlockPos target = breakCache.secondPos != null ? breakCache.secondPos : breakCache.bedPos;
            if (target != null) {
                float[] rot = getRotation(target);
                RotationComponent.setRotations(rot[0], rot[1], 180, 180);
                rotate = true;
            }
        }

        if (breakCache.bedPos == null) {
            BlockPos bedPos = findBedsInRange(mc.player.blockPosition(), (int) breakRadius.getProperty().floatValue());
            if (bedPos != null) {
                BlockState bedState = mc.level.getBlockState(bedPos);
                if (bedState.getBlock() instanceof BedBlock) {
                    breakCache.bedPos = bedPos;
                    BlockPos otherPos = bedState.getValue(BedBlock.PART) == BedPart.HEAD
                            ? bedPos.relative(bedState.getValue(BedBlock.FACING).getOpposite())
                            : bedPos.relative(bedState.getValue(BedBlock.FACING));
                    if (isAccessible(bedPos) && isAccessible(otherPos) && breakBlockSurrounding.getProperty()) {
                        breakCache.secondPos = findBestBlocktoBreak(bedPos, otherPos);
                    } else {
                        breakCache.secondPos = null;
                    }
                    breakCache.tick = 0;
                }
            }
        }

        if (breakCache.bedPos != null && !mc.level.getBlockState(breakCache.bedPos).isAir() &&
                (breakRadius.getProperty().floatValue() * 10) >= mc.player.position().distanceTo(
                        new Vec3(breakCache.bedPos.getX() + 0.5, breakCache.bedPos.getY(), breakCache.bedPos.getZ() + 0.5))) {
            breakCache.isBreaking = true;

            if (breakCache.secondPos != null && !mc.level.getBlockState(breakCache.secondPos).isAir() &&
                    (breakRadius.getProperty().floatValue() * 10) >= mc.player.position().distanceTo(
                            new Vec3(breakCache.secondPos.getX() + 0.5, breakCache.secondPos.getY(), breakCache.secondPos.getZ() + 0.5))) {
                breakBlock(breakCache.secondPos, true);
            } else {
                if (breakCache.secondProgress && breakCache.secondPos != null) {
                    finalizeBlockBreak(breakCache.secondPos, false);
                    breakCache.secondPos = null;
                    breakCache.secondProgress = false;
                    breakCache.tick = 0;
                }
                breakBlock(breakCache.bedPos, false);
            }
        } else if (breakCache.isBreaking) {
            finalizeBlockBreak(breakCache.secondPos, true);
            finalizeBlockBreak(breakCache.bedPos, true);
            breakCache.reset();
            if (breakCache.originalSlot != -1 && mc.player != null && breakCache.originalSlot != mc.player.getInventory().getSelectedSlot()) {
                PacketUtil.sendNoEvent(new ServerboundSetCarriedItemPacket(breakCache.originalSlot));
                mc.player.getInventory().setSelectedSlot(breakCache.originalSlot);
            }
        }
    }

    private void breakBlock(BlockPos blockPos, boolean isSecondPos) {
        if (mc.player == null || mc.level == null) return;

        doAutoTool(blockPos);

        boolean currentProgress = isSecondPos ? breakCache.secondProgress : breakCache.bedProgress;
        final String barName = "Breaking: " + (isSecondPos ? "Support" : "Bed");

        if (!currentProgress) {
            breakCache.maxTicks = PlayerUtil.getBreakTicks(blockPos, mc.player.getMainHandItem());
            breakCache.tick = 0;
            ChatUtil.printDebug("Starting break: " + blockPos + " | Max ticks: " + breakCache.maxTicks);
        }

        ProgressBarComponent.createBar(barName);
        final float progress = Math.min(1.0f, breakCache.tick / breakCache.maxTicks);
        ProgressBarComponent.updateBar(barName, progress);

        if (!currentProgress) {
            if (rotations.getProperty() && !keepRotation.getProperty()) {
                float[] rot = getRotation(blockPos);
                RotationComponent.setRotations(rot[0], rot[1], 180, 180);
                rotate = true;
            }

            PacketUtil.sendNoEvent(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
            if (!keepRotation.getProperty()) rotate = false;

            if (isSecondPos) {
                breakCache.secondProgress = true;
            } else {
                breakCache.bedProgress = true;
            }
        } else {
            if (!mc.level.getBlockState(blockPos).isAir()) {
                breakCache.tick++;
            }

            destroyBlock(new BlockHitResult(
                    new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5),
                    Direction.UP, blockPos, false
            ));

            mc.level.destroyBlockProgress(
                    mc.player.getId(),
                    blockPos,
                    (int) (progress * 10)
            );
        }
    }

    private void finalizeBlockBreak(BlockPos blockPos, boolean reset) {
        if (blockPos == null || mc.player == null || mc.level == null) return;

        ProgressBarComponent.clear();

        if (mc.level.getBlockState(blockPos).isAir()) {
            if (rotations.getProperty() && !keepRotation.getProperty()) {
                float[] rot = getRotation(blockPos);
                RotationComponent.setRotations(rot[0], rot[1], 180, 180);
                rotate = true;
            }

            PacketUtil.sendNoEvent(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
            if (!keepRotation.getProperty()) rotate = false;

            mc.player.swing(InteractionHand.MAIN_HAND);
            mc.gameMode.destroyBlock(blockPos);
        } else {
            PacketUtil.sendNoEvent(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, blockPos, Direction.UP));
            rotate = false;
        }

        if (reset) {
            breakCache.tick = 0;
            breakCache.maxTicks = 0;
        }
    }

    private BlockPos findBedsInRange(BlockPos playerPos, int radius) {
        BlockPos closestBedPos = null;
        double minDistance = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockState blockState = mc.level.getBlockState(pos);
                    if (blockState.getBlock() instanceof BedBlock) {
                        if (whitelistOwn.getProperty() && BedWhiteListComponent.isWhitelisted(pos)) {
                            continue;
                        }

                        double distance = mc.player.position().distanceTo(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                        boolean notUserBed = (userBed == null) || (!pos.equals(userBed));
                        if (distance < minDistance && (breakRadius.getProperty().floatValue() * 10) >= distance && notUserBed) {
                            minDistance = distance;
                            closestBedPos = pos;
                        }
                    }
                }
            }
        }

        return closestBedPos;
    }

    private BlockPos findBestBlocktoBreak(BlockPos headPos, BlockPos footPos) {
        BlockPos[] surroundingBlocks = {
                headPos.above(), headPos.north(), headPos.south(), headPos.east(), headPos.west(),
                footPos.above(), footPos.north(), footPos.south(), footPos.east(), footPos.west()
        };
        BlockPos fastestPos = null;
        float minBreakTicks = Float.MAX_VALUE;
        double minDistance = Double.MAX_VALUE;
        java.util.List<BlockPos> fastestBlocks = new java.util.ArrayList<>();

        for (BlockPos pos : surroundingBlocks) {
            if (whitelistOwn.getProperty() && BedWhiteListComponent.isWhitelisted(pos)) {
                continue;
            }

            BlockState state = mc.level.getBlockState(pos);
            if (!state.isAir() && !(state.getBlock() instanceof BedBlock)) {
                float breakTicks = PlayerUtil.getBreakTicks(pos, mc.player.getMainHandItem());
                double distance = mc.player.position().distanceTo(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                if (breakTicks < minBreakTicks && (breakRadius.getProperty().floatValue() * 10) >= distance) {
                    minBreakTicks = breakTicks;
                    fastestBlocks.clear();
                    fastestBlocks.add(pos);
                    minDistance = distance;
                } else if (breakTicks == minBreakTicks && (breakRadius.getProperty().floatValue() * 10) >= distance) {
                    fastestBlocks.add(pos);
                    minDistance = Math.min(minDistance, distance);
                }
            }
        }

        for (BlockPos pos : fastestBlocks) {
            double distance = mc.player.position().distanceTo(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            if (distance <= minDistance) {
                minDistance = distance;
                fastestPos = pos;
            }
        }

        return fastestPos;
    }

    private void doAutoTool(BlockPos pos) {
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
            if (breakCache.originalSlot == -1) {
                breakCache.originalSlot = mc.player.getInventory().getSelectedSlot();
            }
            mc.player.getInventory().setSelectedSlot(bestToolSlot);
        }
    }

    private float[] getRotation(BlockPos pos) {
        double diffX = pos.getX() + 0.5 - mc.player.getX();
        double diffY = pos.getY() + 0.5 - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = pos.getZ() + 0.5 - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float pitch = mc.player.getXRot() + Mth.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getXRot());
        float yaw = mc.player.getYRot() + Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f - mc.player.getYRot());

        float[] rots = new float[]{yaw, pitch};
        float[] lastRots = new float[]{RotationComponent.getFakeYaw(), RotationComponent.getFakePitch()};

        return RotationUtil.getFixedRotations(rots, lastRots);
    }

    private void destroyBlock(BlockHitResult rayTraceResult) {
        Direction direction = rayTraceResult.getDirection();
        BlockPos blockPos = rayTraceResult.getBlockPos();

        if (mc.gameMode.continueDestroyBlock(blockPos, direction)) {
            mc.player.swing(InteractionHand.MAIN_HAND);
            // todo: fix
            /*mc.particleManager.addBlockBreakingParticles(blockPos, direction);*/
        }
    }

    private boolean isAccessible(BlockPos pos) {
        if (mc.level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (mc.level.getBlockState(pos.relative(direction)).isAir()) {
                return false;
            }
        }
        return true;
    }
}