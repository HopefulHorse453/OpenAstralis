package cc.astralis.module.impl.client.cheaterfinder;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MotionPredictor {
    private Vec3 lastPos = Vec3.ZERO;
    private Vec3 secondLastPos = Vec3.ZERO;
    private Vec3 lastVelocity = Vec3.ZERO;

    public static final double GRAVITY = 0.08;
    public static final double AIR_RESISTANCE = 0.91;
    public static final double WATER_RESISTANCE = 0.8;
    public static final double LAVA_RESISTANCE = 0.5;
    public static final double GROUND_FRICTION = 0.6;
    public static final double SPRINT_MODIFIER = 1.3;
    public static final double SNEAK_MODIFIER = 0.3;
    public static final double JUMP_MOTION = 0.42;
    public static final double MIN_MOVEMENT_THRESHOLD = 0.003;
    public static final double MOVEMENT_EPSILON = 0.0001;

    public Vec3 predictNextPosition(Player player, PlayerData playerData) {
        Vec3 currentPos = new Vec3(player.getX(), player.getY(), player.getZ());
        Vec3 currentVelocity = calculateVelocity(currentPos);

        double expectedX = currentPos.x + currentVelocity.x;
        double expectedY = calculateExpectedY(player, currentVelocity);
        double expectedZ = currentPos.z + currentVelocity.z;

        Vec3 predicted = new Vec3(expectedX, expectedY, expectedZ);
        predicted = applyMovementModifiers(player, predicted, currentVelocity);

        updatePositionHistory(currentPos);
        return predicted;
    }

    private Vec3 calculateVelocity(Vec3 currentPos) {
        if (lastPos.equals(Vec3.ZERO)) {
            return Vec3.ZERO;
        }

        Vec3 newVelocity = currentPos.subtract(lastPos);

        if (!lastVelocity.equals(Vec3.ZERO)) {
            newVelocity = new Vec3(
                    (newVelocity.x * 0.7 + lastVelocity.x * 0.3),
                    (newVelocity.y * 0.7 + lastVelocity.y * 0.3),
                    (newVelocity.z * 0.7 + lastVelocity.z * 0.3)
            );
        }

        if (Math.abs(newVelocity.x) < MIN_MOVEMENT_THRESHOLD) newVelocity = new Vec3(0, newVelocity.y, newVelocity.z);
        if (Math.abs(newVelocity.y) < MIN_MOVEMENT_THRESHOLD) newVelocity = new Vec3(newVelocity.x, 0, newVelocity.z);
        if (Math.abs(newVelocity.z) < MIN_MOVEMENT_THRESHOLD) newVelocity = new Vec3(newVelocity.x, newVelocity.y, 0);

        lastVelocity = newVelocity;
        return newVelocity;
    }

    private double calculateExpectedY(Player player, Vec3 velocity) {
        double expectedY = player.getY();

        if (player.onGround()) {
            if (isLikelyJumping(player, velocity)) {
                expectedY += JUMP_MOTION;
            } else if (velocity.y > 0) {
                expectedY += velocity.y - GRAVITY;
            }
        } else {
            expectedY += (velocity.y - GRAVITY) * AIR_RESISTANCE;
        }

        return expectedY;
    }

    private boolean isLikelyJumping(Player player, Vec3 velocity) {
        return player.onGround() && velocity.y > 0.1 && !player.isShiftKeyDown();
    }

    private Vec3 applyMovementModifiers(Player player, Vec3 predicted, Vec3 velocity) {
        double modifiedX = predicted.x;
        double modifiedZ = predicted.z;

        double movementFactor = getBaseMovementFactor(player);

        if (player.isInWater()) movementFactor *= WATER_RESISTANCE;
        if (player.isInLava()) movementFactor *= LAVA_RESISTANCE;

        if (player.isSprinting()) movementFactor *= SPRINT_MODIFIER;
        if (player.isShiftKeyDown()) movementFactor *= SNEAK_MODIFIER;

        boolean isMoving = velocity.horizontalDistanceSqr() > MOVEMENT_EPSILON;

        if (isMoving) {
            modifiedX = predicted.x + (velocity.x * movementFactor);
            modifiedZ = predicted.z + (velocity.z * movementFactor);
        } else {
            modifiedX = predicted.x + (velocity.x * movementFactor * 0.2);
            modifiedZ = predicted.z + (velocity.z * movementFactor * 0.2);
        }

        return new Vec3(modifiedX, predicted.y, modifiedZ);
    }

    private double getBaseMovementFactor(Player player) {
        if (player.onGround()) {
            return GROUND_FRICTION;
        }
        return AIR_RESISTANCE;
    }

    public boolean isPositionSuspicious(Vec3 actualPos, Vec3 predictedPos, double threshold) {
        double deviation = actualPos.distanceTo(predictedPos);
        return deviation > threshold;
    }

    public double calculateDeviation(Vec3 actualPos, Vec3 predictedPos) {
        return actualPos.distanceTo(predictedPos);
    }

    public double getHorizontalDeviation(Vec3 actualPos, Vec3 predictedPos) {
        double deltaX = actualPos.x - predictedPos.x;
        double deltaZ = actualPos.z - predictedPos.z;
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

    public double getVerticalDeviation(Vec3 actualPos, Vec3 predictedPos) {
        return Math.abs(actualPos.y - predictedPos.y);
    }

    private void updatePositionHistory(Vec3 currentPos) {
        secondLastPos = lastPos;
        lastPos = currentPos;
    }

    public void reset() {
        lastPos = Vec3.ZERO;
        secondLastPos = Vec3.ZERO;
        lastVelocity = Vec3.ZERO;
    }
}