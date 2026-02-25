package cc.astralis.util.player;

import cc.astralis.Astralis;
import cc.astralis.interfaces.IAccess;
import cc.astralis.module.impl.client.RotationsModule;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public class RayTraceUtil implements IAccess {
    // todo: white list from obfuscation.
    public static boolean getOver(final Direction enumFacing, final BlockPos pos, final boolean strict, float reach, float yaw, float pitch) {
        BlockHitResult hitResult = raytrace(yaw, pitch, reach);

        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK)
            return false;

        final BlockPos hitPos = hitResult.getBlockPos();

        if (hitPos == null)
            return false;

        return hitPos.equals(pos) && (!strict || hitResult.getDirection() == enumFacing);
    }

    public static boolean overBlock(float yaw, float pitch, float reach) {
        return raytrace(yaw, pitch, reach).getType() == HitResult.Type.BLOCK;
    }

    public static BlockHitResult raytrace(float yaw, float pitch, float reach) {
        final Vec3 vec3 = mc.getCameraEntity().getEyePosition(1.0f);
        final Vec3 vec4 = getVectorForRotation(yaw, pitch);
        final Vec3 vec5 = vec3.add(vec4.x * reach, vec4.y * reach, vec4.z * reach);

        return mc.level.clip(new ClipContext(
                vec3,
                vec5,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.getCameraEntity()
        ));
    }

    public static Vec3 getVectorForRotation(float yaw, float pitch) {
        final float f = Mth.cos(-yaw * 0.017453292f - 3.1415927f);
        final float f2 = Mth.sin(-yaw * 0.017453292f - 3.1415927f);
        final float f3 = -Mth.cos(-pitch * 0.017453292f);
        final float f4 = Mth.sin(-pitch * 0.017453292f);
        return new Vec3((f2 * f3), f4, (f * f3));
    }

    // dog shit
    public static boolean isLookingAtBlock(Direction facing, BlockPos position, boolean strict, float reach, float yaw, float pitch) {
        BlockHitResult blockHitResult = rayTrace(reach, yaw, pitch);
        if (blockHitResult == null) {
            return false;
        }

        if (blockHitResult.getBlockPos().getX() == position.getX() && blockHitResult.getBlockPos().getY() == position.getY() && blockHitResult.getBlockPos().getZ() == position.getZ()) {
            if (strict) {
                return blockHitResult.getDirection() == facing;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public static BlockHitResult rayTrace(float reach, float yaw, float pitch) {
        final Vec3 vec3 = mc.getCameraEntity().getEyePosition(1.0f);
        final Vec3 vec4 = getVectorForRotation(yaw, pitch);
        final Vec3 vec5 = vec3.add(vec4.x * reach, vec4.y * reach, vec4.z * reach);

        return mc.level.clip(new ClipContext(vec3, vec5, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.getCameraEntity()));
    }

    @Getter
    private static EntityHitResult hitResult = null;

    // mc code
    public static void updateCrosshairTarget(boolean throughWalls) {
        final float tickProgress = 1.0f;

        Entity entity = mc.getCameraEntity();
        if (entity != null) {
            if (mc.level != null && mc.player != null) {
                Profiler.get().push("pick");
                double d = mc.player.blockInteractionRange();
                double e = mc.player.entityInteractionRange();
                HitResult foundHitResult = findCrosshairTarget(entity, d, e, tickProgress, throughWalls);

                if (foundHitResult instanceof EntityHitResult entityHitResult) {
                    hitResult = entityHitResult;
                } else {
                    hitResult = null;
                }

                /*    mc.targetedEntity = var10001;*/
                Profiler.get().pop();
            }
        }
    }

    private static HitResult findCrosshairTarget(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickProgress, boolean ignoreBlocks) {
        double d = Math.max(blockInteractionRange, entityInteractionRange);
        double e = Mth.square(d);
        Vec3 vec3d = camera.getEyePosition(tickProgress);

        HitResult hitResult = ignoreBlocks
                ? BlockHitResult.miss(
                vec3d.add(camera.getViewVector(tickProgress).scale(d)),
                Direction.getApproximateNearest(
                        camera.getViewVector(tickProgress).x,
                        camera.getViewVector(tickProgress).y,
                        camera.getViewVector(tickProgress).z
                ),
                BlockPos.containing(vec3d.add(camera.getViewVector(tickProgress).scale(d)))
        ) : camera.pick(d, tickProgress, false);

        double f = hitResult.getLocation().distanceToSqr(vec3d);
        if (hitResult.getType() != HitResult.Type.MISS) {
            e = f;
            d = Math.sqrt(f);
        }

        Vec3 vec3d2 = camera.getViewVector(tickProgress);
        Vec3 vec3d3 = vec3d.add(vec3d2.scale(d));
        AABB box = camera.getBoundingBox().expandTowards(vec3d2.scale(d)).inflate(1.0f);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(camera, vec3d, vec3d3, box, EntitySelector.CAN_BE_PICKED, e);

        return entityHitResult != null && entityHitResult.getLocation().distanceToSqr(vec3d) < f
                ? ensureTargetInRange(entityHitResult, vec3d, entityInteractionRange)
                : ensureTargetInRange(hitResult, vec3d, blockInteractionRange);
    }

    private static HitResult ensureTargetInRange(HitResult hitResult, Vec3 cameraPos, double interactionRange) {
        Vec3 vec3d = hitResult.getLocation();
        if (!vec3d.closerThan(cameraPos, interactionRange)) {
            Vec3 vec3d2 = hitResult.getLocation();
            Direction direction = Direction.getApproximateNearest(vec3d2.x - cameraPos.x, vec3d2.y - cameraPos.y, vec3d2.z - cameraPos.z);
            return BlockHitResult.miss(vec3d2, direction, BlockPos.containing(vec3d2));
        } else {
            return hitResult;
        }
    }
}
