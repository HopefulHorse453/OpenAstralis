package cc.astralis.util.player;

import cc.astralis.Astralis;
import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.interfaces.IAccess;
import cc.astralis.module.impl.client.RotationsModule;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;
import java.util.List;

public class RotationUtil implements IAccess {

    public static float smoothRotation(float from, float to, float speed) {
        float f = Mth.wrapDegrees(to - from);

        if (f > speed) {
            f = speed;
        }

        if (f < -speed) {
            f = -speed;
        }

        return from + f;
    }

    public static float[] getFixedRotations(float[] rotations, float[] lastRotations) {
        float yaw = rotations[0];
        float pitch = rotations[1];

        float lastYaw = lastRotations[0];
        float lastPitch = lastRotations[1];

        float f = (float) (mc.options.sensitivity().get() * 0.6F + 0.2F);
        float gcd = f * f * f * 1.2F;

        float deltaYaw = yaw - lastYaw;
        float deltaPitch = pitch - lastPitch;

        float fixedDeltaYaw = deltaYaw - (deltaYaw % gcd);
        float fixedDeltaPitch = deltaPitch - (deltaPitch % gcd);

        float fixedYaw = lastYaw + fixedDeltaYaw;
        float fixedPitch = lastPitch + fixedDeltaPitch;

        return new float[]{fixedYaw, fixedPitch};
    }

    public static Vec3 getHitVec3(Entity entity) {
        Vec3 eyesPosition = getPositionEyes();
        float size = Astralis.getInstance().getModuleManager().getModule(RotationsModule.class).oldHitBoxOffset.getProperty() ? 0.1F : 0.0F;
        AABB entityBoundingBox = entity.getBoundingBox().inflate(size);

        double x = Mth.clamp(eyesPosition.x(), entityBoundingBox.minX, entityBoundingBox.maxX);
        double y = Mth.clamp(eyesPosition.y(), entityBoundingBox.minY, entityBoundingBox.maxY);
        double z = Mth.clamp(eyesPosition.z(), entityBoundingBox.minZ, entityBoundingBox.maxZ);

        return new Vec3(x, y, z);
    }

    public static float[] getRotations(Entity entity) {
        Vec3 vec3 = getHitVec3(entity);

        return getRotationsToVector(vec3);
    }

    public static float[] getRotationsToVector(Vec3 vec3d) {
        double x = vec3d.x - (mc.player.getX());
        double y = vec3d.y - getPositionEyes().y;
        double z = vec3d.z - (mc.player.getZ());

        final double theta = Mth.sqrt((float) (x * x + z * z));

        final float yaw = (float) (Math.atan2(z, x) * 180.0 / Math.PI - 90.0);
        final float pitch = (float) (-(Math.atan2(y, theta) * 180.0 / Math.PI));

        return new float[]{
                (mc.player.getYRot() + Mth.wrapDegrees(yaw - mc.player.getYRot())) % 360,
                (mc.player.getXRot() + Mth.wrapDegrees(pitch - mc.player.getXRot())) % 360
        };
    }

    public static Vec3 getPositionEyes() {
        float eyeHeight =
                Astralis.getInstance().getModuleManager().getModule(RotationsModule.class).modernHitVec.getProperty() ?
                        mc.player.getEyeHeight(mc.player.getPose()) : mc.player.getBbHeight() * 0.85F;

        return new Vec3(mc.player.getX(), mc.player.getY() + eyeHeight, mc.player.getZ());
    }

    /*     public static double getDistanceToEntityBox(Entity entity, BodyPart bodyPart) {
        Vec3d eyes = getPositionEyes();
        Vec3d eyesPosition = getPositionEyes();
        float size = Astralis.getInstance().getModuleManager().getModule(RotationsModule.class).oldHitBoxOffset.getProperty() ? 0.1F : 0.0F;
        Box entityBoundingBox = entity.getBoundingBox().expand(size);

        double x = MathHelper.clamp(eyesPosition.getX(), entityBoundingBox.minX, entityBoundingBox.maxX);
        double y = MathHelper.clamp(eyesPosition.getY(), entityBoundingBox.minY, entityBoundingBox.maxY);
        double z = MathHelper.clamp(eyesPosition.getZ(), entityBoundingBox.minZ, entityBoundingBox.maxZ);

        Vec3d pos = new Vec3d(x, y - 0.4, z);
        double xDist = Math.abs(pos.x - eyes.x);
        double yDist = Math.abs(pos.y - eyes.y);
        double zDist = Math.abs(pos.z - eyes.z);

        return Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2) + Math.pow(zDist, 2));
    } */

    public static double getDistanceToEntityBox(Entity entity) {
        Vec3 eyesPos = mc.player.getEyePosition(1.0F);
        return eyesPos.distanceTo(getNearestPointBB(eyesPos, entity.getBoundingBox()));
    }

    public static Vec3 getNearestPointBB(Vec3 eye, AABB box) {
        double[] origin = new double[]{eye.x, eye.y, eye.z};
        double[] destMins = new double[]{box.minX, box.minY, box.minZ};
        double[] destMaxs = new double[]{box.maxX, box.maxY, box.maxZ};

        for (int i = 0; i <= 2; i++) {
            if (origin[i] > destMaxs[i]) {
                origin[i] = destMaxs[i];
            } else if (origin[i] < destMins[i]) {
                origin[i] = destMins[i];
            }
        }

        return new Vec3(origin[0], origin[1], origin[2]);
    }

    public static double getDistanceToPos(Vec3 pos) {
        return pos.distanceTo(getPositionEyes());
    }

    public static BlockHitResult calculateIntercept(AABB targetBox, Vec3 eyesPos, Vec3 hitVec) {
        // records are low-key cool asf, since u can create them inside of methods !
        record Intersection(Vec3 vec, Direction face) {
            /* w */
        }

        var intersections = List.of(
                new Intersection(getIntermediateWithXValue(eyesPos, hitVec, targetBox.minX), Direction.WEST),
                new Intersection(getIntermediateWithXValue(eyesPos, hitVec, targetBox.maxX), Direction.EAST),
                new Intersection(getIntermediateWithYValue(eyesPos, hitVec, targetBox.minY), Direction.DOWN),
                new Intersection(getIntermediateWithYValue(eyesPos, hitVec, targetBox.maxY), Direction.UP),
                new Intersection(getIntermediateWithZValue(eyesPos, hitVec, targetBox.minZ), Direction.NORTH),
                new Intersection(getIntermediateWithZValue(eyesPos, hitVec, targetBox.maxZ), Direction.SOUTH)
        );

        var validIntersections = intersections.stream()
                .filter(intersection -> intersection.vec() != null)
                .filter(intersection -> switch (intersection.face()) {
                    case WEST, EAST -> isVecInYZ(targetBox, intersection.vec());
                    case DOWN, UP -> isVecInXZ(targetBox, intersection.vec());
                    case NORTH, SOUTH -> isVecInXY(targetBox, intersection.vec());
                })
                .toList();

        var closest = validIntersections.stream()
                .min(Comparator.comparingDouble(i -> eyesPos.distanceToSqr(i.vec())))
                .orElse(null);

        if (closest == null)
            return null;

        return new BlockHitResult(closest.vec(), closest.face(), BlockPos.ZERO, false);
    }

    public static Vec3 getIntermediateWithXValue(Vec3 start, Vec3 end, double x) {
        return getIntermediate(start, end, Axis.X, x);
    }

    public static Vec3 getIntermediateWithYValue(Vec3 start, Vec3 end, double y) {
        return getIntermediate(start, end, Axis.Y, y);
    }

    public static Vec3 getIntermediateWithZValue(Vec3 start, Vec3 end, double z) {
        return getIntermediate(start, end, Axis.Z, z);
    }

    private static Vec3 getIntermediate(Vec3 start, Vec3 end, Axis axis, double coord) {
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double dz = end.z - start.z;

        double delta = switch (axis) {
            case X -> dx;
            case Y -> dy;
            case Z -> dz;
        };

        if (Math.abs(delta) < 1e-7) return null;

        double t = switch (axis) {
            case X -> (coord - start.x) / dx;
            case Y -> (coord - start.y) / dy;
            case Z -> (coord - start.z) / dz;
        };

        if (t < 0.0 || t > 1.0) return null;

        return new Vec3(
                start.x + dx * t,
                start.y + dy * t,
                start.z + dz * t
        );
    }

    private enum Axis {
        X, Y, Z
    }

    private static boolean isVecInYZ(AABB box, Vec3 vec) {
        return vec != null
                && vec.y >= box.minY && vec.y <= box.maxY
                && vec.z >= box.minZ && vec.z <= box.maxZ;
    }

    private static boolean isVecInXZ(AABB box, Vec3 vec) {
        return vec != null
                && vec.x >= box.minX && vec.x <= box.maxX
                && vec.z >= box.minZ && vec.z <= box.maxZ;
    }

    private static boolean isVecInXY(AABB box, Vec3 vec) {
        return vec != null
                && vec.x >= box.minX && vec.x <= box.maxX
                && vec.y >= box.minY && vec.y <= box.maxY;
    }

    public static Vec3 getCurrentHitVec(double range) {
        UVPair rotation = new UVPair(
                RotationComponent.getYaw(),
                RotationComponent.getPitch()
        );

        Vec3 eyePos = mc.player.getEyePosition(1.0F);
        Vec3 rotationVec = getVectorForRotation(rotation);

        return eyePos.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
    }

    public static Vec3 getVectorForRotation(UVPair rotation) {
        float yawCos = (float) Math.cos(-rotation.u() * (float) (Math.PI / 180.0) - (float) Math.PI);
        float yawSin = (float) Math.sin(-rotation.u() * (float) (Math.PI / 180.0) - (float) Math.PI);
        float pitchCos = (float) (-Math.cos(-rotation.v() * (float) (Math.PI / 180.0)));
        float pitchSin = (float) Math.sin(-rotation.v() * (float) (Math.PI / 180.0));

        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    public static Entity rayCastEntity(double range) {
        Entity renderViewEntity = mc.getCameraEntity();
        float yaw = RotationComponent.getYaw();
        float pitch = RotationComponent.getPitch();
        if (renderViewEntity != null && mc.level != null) {
            double blockReachDistance = range;
            Vec3 eyePosition = renderViewEntity.getEyePosition(1.0F);
            float yawCos = (float)Math.cos(-yaw * (float) (Math.PI / 180.0) - Math.PI);
            float yawSin = (float)Math.sin(-yaw * (float) (Math.PI / 180.0) - Math.PI);
            float pitchCos = (float)(-Math.cos(-pitch * (float) (Math.PI / 180.0)));
            float pitchSin = (float)Math.sin(-pitch * (float) (Math.PI / 180.0));
            Vec3 entityLook = new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
            Vec3 vector = eyePosition.add(entityLook.x * range, entityLook.y * range, entityLook.z * range);
            List<Entity> entityList = mc.level
                    .getEntities(
                            renderViewEntity,
                            renderViewEntity.getBoundingBox().inflate(entityLook.x * range, entityLook.y * range, entityLook.z * range).inflate(1.0, 1.0, 1.0),
                            entityx -> entityx != null && ( !entityx.isSpectator()) && entityx.isPickable()
                    );
            Entity pointedEntity = null;

            for (Entity entity : entityList) {
                double collisionBorderSize = 0.0;
                AABB axisAlignedBB = entity.getBoundingBox().inflate(collisionBorderSize, collisionBorderSize, collisionBorderSize);
                BlockHitResult movingObjectPosition = calculateIntercept(axisAlignedBB, eyePosition, vector);
                if (axisAlignedBB.contains(eyePosition)) {
                    if (blockReachDistance >= 0.0) {
                        pointedEntity = entity;
                        blockReachDistance = 0.0;
                    }
                } else if (movingObjectPosition != null) {
                    double eyeDistance = eyePosition.distanceTo(movingObjectPosition.getLocation());
                    if (eyeDistance < blockReachDistance || blockReachDistance == 0.0) {
                        if (entity != renderViewEntity.getVehicle() || renderViewEntity.isPickable()) {
                            pointedEntity = entity;
                            blockReachDistance = eyeDistance;
                        } else if (blockReachDistance == 0.0) {
                            pointedEntity = entity;
                        }
                    }
                }
            }

            return pointedEntity;
        } else {
            return null;
        }
    }

    public static float forwardYaw() {
        final Vec3 v = mc.player.getDeltaMovement();

        if (v.lengthSqr() > 1.0E-6) {
            float yaw = (float) Math.toDegrees(Math.atan2(-v.x, v.z));
            return Mth.wrapDegrees(yaw);
        }

        float fwd = mc.player.input.getMoveVector().x;
        float str = mc.player.input.getMoveVector().y;
        if (fwd != 0 || str != 0) {
            double local = Math.toDegrees(Math.atan2(str, fwd));
            float yaw = mc.player.getYRot() + (float) local;
            return Mth.wrapDegrees(yaw);
        }

        return Mth.wrapDegrees(mc.player.getYRot());
    }

    public static float[] getRotationFromPosition(double x, double y, double z) {
        double deltaX = x - mc.player.getX();
        double deltaZ = z - mc.player.getZ();
        double deltaY = y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double horizontalDist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, horizontalDist));

        return new float[]{yaw, pitch};
    }

    public static float[] getDirectionToBlock(BlockPos blockPos, Direction direction) {
        Vec3 blockCenter = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
        Vec3 directionOffset = new Vec3(direction.getUnitVec3i()).scale(0.25);
        Vec3 targetPos = blockCenter.add(directionOffset);

        return getRotationFromPosition(targetPos.x, targetPos.y, targetPos.z);
    }

    public static float getYawBasedPitch(BlockPos blockPos, Direction direction, float currentYaw, float lastPitch,
                                         int maxPitch) {
        for (float i = 30; i <= maxPitch; i += 0.1f + Math.random() / 10) {
            if (RayTraceUtil.getOver(direction, blockPos, true, 5, currentYaw, i)) {
                return i;
            }
        }

        return lastPitch;
    }
}
