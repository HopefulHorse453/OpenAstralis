package cc.astralis.module.impl.combat;

import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.*;
import cc.astralis.property.properties.body.BodyPart;
import cc.astralis.util.player.*;
import java.util.Comparator;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AimassistModule extends Module {

    private final ModeProperty mode = new ModeProperty("Mode", "Normal", "Normal", "Silent", "Flex");
    private final NumberProperty range = new NumberProperty("Range", 4.5f, 1.0f, 6.0f, 0.1f);
    private final NumberProperty fov = new NumberProperty("FOV", 90f, 1f, 180f, 1f);
    private final BooleanProperty useSensitivity = new BooleanProperty("Use Sensitivity", true);
    private final BooleanProperty onlyClick = new BooleanProperty("Only While Clicking", false);
    private final BooleanProperty onlyHoldingWeapon = new BooleanProperty("Weapon Only", false);
    private final BooleanProperty ignoreTeammates = new BooleanProperty("Ignore Teammates", false);
    private final BooleanProperty thoughWalls = new BooleanProperty("Though Walls", false);
    private final BooleanProperty stopAimingOnTarget = new BooleanProperty("Stop Aiming on Target", false);
    private final BooleanProperty slowAimIfClose = new BooleanProperty("Slow Aim if Close", true);
    private final NumberProperty slowAimThreshold = new NumberProperty("Slow Aim Threshold", 10f, 1f, 30f, 1f).setVisible(slowAimIfClose::getProperty);
    private final NumberProperty slowAimFactor = new NumberProperty("Slow Aim Factor", 0.3f, 0.1f, 1.0f, 0.1f).setVisible(slowAimIfClose::getProperty);
    private final BooleanProperty takeCurvedWay = new BooleanProperty("Take Curved Way", false);
    private final NumberProperty curveAmplitude = new NumberProperty("Curve Amplitude", 1.5f, 0.5f, 5.0f, 0.1f).setVisible(takeCurvedWay::getProperty);
    private final NumberProperty curveFrequency = new NumberProperty("Curve Frequency", 0.3f, 0.1f, 1.5f, 0.05f).setVisible(takeCurvedWay::getProperty);
    private final NumberProperty minSpeed = new NumberProperty("Min Speed", 3.0f, 1.0f, 15.0f, 0.1f);
    private final NumberProperty maxSpeed = new NumberProperty("Max Speed", 6.0f, 1.0f, 20.0f, 0.1f);
    private final NumberProperty randomAmount = new NumberProperty("Random Amount", 1.0f, 0.0f, 5.0f, 0.1f);
    private final NumberProperty randomFrequency = new NumberProperty("Random Frequency", 0.3f, 0.05f, 2.0f, 0.05f);

    private float curveProgress = 0.0f;
    private int flexTickCounter = 0;
    private boolean doing360 = false;
    private float flexStartYaw;
    private float flexCurrentYaw;
    private float flexTargetYaw;

    public AimassistModule() {
        super(Category.COMBAT);
        registerProperties(mode, range, fov, useSensitivity, minSpeed, maxSpeed, onlyHoldingWeapon, onlyClick, ignoreTeammates,
                thoughWalls, stopAimingOnTarget, slowAimIfClose, slowAimThreshold, slowAimFactor,
                takeCurvedWay, curveAmplitude, curveFrequency, randomAmount, randomFrequency);
    }

    @EventTarget
    public void onUpdate(Render2DEvent event) {
        if ((onlyClick.getProperty() && !mc.options.keyAttack.isDown()) ||
                (onlyHoldingWeapon.getProperty() && !PlayerUtil.isHoldingWeapon())) {
            return;
        }

        Entity target = getClosestTarget(range.getProperty().floatValue());
        if (target == null) {
            curveProgress = 0.0f; // Reset
            return;
        }

        BodyPart bodyPart = getClosestBodyPartToCrosshair(target);
        // erm i kinda broke this sry ihassedich.
        float[] rotations = RotationUtil.getRotations(target);
        if (getAngleDifference(rotations[0], mc.player.getYRot()) > fov.getProperty().floatValue()) {
            curveProgress = 0.0f; // Reset
            return;
        }

        if (stopAimingOnTarget.getProperty() && mc.hitResult instanceof EntityHitResult hit && hit.getEntity() == target) {
            curveProgress = 0.0f; // Reset
            return;
        }

        float baseSpeed = (minSpeed.getProperty().floatValue() + maxSpeed.getProperty().floatValue()) / 2.0f;

        float speedScale = 1.0f;
        if (useSensitivity.getProperty()) {
            float sensitivity = mc.options.sensitivity().get().floatValue();
            speedScale = sensitivity * 2.0f;
        }

        float speedX = baseSpeed * 0.05f * speedScale;
        float speedY = baseSpeed * 0.05f * speedScale;

        if (slowAimIfClose.getProperty()) {
            float angleDiff = getAngleDifference(rotations[0], mc.player.getYRot()) + getAngleDifference(rotations[1], mc.player.getXRot());
            if (angleDiff < slowAimThreshold.getProperty().floatValue()) {
                float scale = Math.max(slowAimFactor.getProperty().floatValue(), angleDiff / slowAimThreshold.getProperty().floatValue());
                speedX *= scale;
                speedY *= scale;
            }
        }

        if (takeCurvedWay.getProperty()) {
            curveProgress += curveFrequency.getProperty().floatValue();
            if (curveProgress > 1.0f) curveProgress = 1.0f;
        } else {
            curveProgress = 0.0f;
        }

        switch (mode.getProperty()) {
            case "Normal" -> adjustAim(getTargetVec3d(target, bodyPart), speedX, speedY, target);
            case "Silent" ->
                    RotationComponent.setRotations(rotations, minSpeed.getProperty().floatValue(), maxSpeed.getProperty().floatValue());
            case "Flex" -> handleFlexMode(target, bodyPart);
        }
    }

    private void handleFlexMode(Entity target, BodyPart bodyPart) {
        float[] lookRotations = RotationUtil.getRotations(target);
        float yawToTarget = lookRotations[0];
        float pitchToTarget = lookRotations[1];

        if (doing360) {
            float speed = 6f;
            flexCurrentYaw += speed;

            if (flexCurrentYaw >= flexTargetYaw) {
                doing360 = false;
            }

            mc.player.setYRot(flexCurrentYaw % 360f); // wrap around
            mc.player.setXRot(pitchToTarget);
            return;
        }

        mc.player.setYRot(yawToTarget);
        mc.player.setXRot(pitchToTarget);

        flexTickCounter++;
        if (flexTickCounter >= 15) {
            doing360 = true;
            flexTickCounter = 0;
            flexStartYaw = mc.player.getYRot();
            flexCurrentYaw = flexStartYaw;
            flexTargetYaw = flexStartYaw + 360f;
        }
    }


    private void adjustAim(Vec3 target, float speedX, float speedY, Entity targetEntity) {
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 delta = target.subtract(eyePos);

        double yaw = Mth.atan2(delta.z, delta.x) * 180 / Math.PI - 90;
        double pitch = -Mth.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)) * 180 / Math.PI;

        double secondYaw = Mth.wrapDegrees(yaw - mc.player.getYRot());
        double secondPitch = pitch - mc.player.getXRot();

        if (takeCurvedWay.getProperty()) {
            float angleDiff = getAngleDifference((float) yaw, mc.player.getYRot()) + getAngleDifference((float) pitch, mc.player.getXRot());
            float amplitude = curveAmplitude.getProperty().floatValue() * Math.min(1.0f, angleDiff / slowAimThreshold.getProperty().floatValue());

            // Do math to get the best direction for the curve
            Vec3 targetVelocity = targetEntity.getDeltaMovement();
            float direction = (float) Math.signum(targetVelocity.x * delta.z - targetVelocity.z * delta.x);
            direction = direction == 0 ? 1 : direction; // Default

            secondYaw += Math.sin(curveProgress * Math.PI) * amplitude * direction;
            secondPitch += Math.cos(curveProgress * Math.PI) * amplitude * 0.5f; // Math.cos to cuz to make pitch more "Legit"
        }

        if (randomAmount.getProperty().floatValue() > 0) {
            float randAmount = randomAmount.getProperty().floatValue();
            float randFreq = randomFrequency.getProperty().floatValue();
            float time = (System.currentTimeMillis() % 100000L) / 1000.0f;

            secondYaw += Math.sin(time * randFreq * 2 * Math.PI) * randAmount;
            secondPitch += Math.cos(time * randFreq * 2 * Math.PI) * randAmount * 0.5f;
        }

        float newYaw = (float) (mc.player.getYRot() + secondYaw * speedX * 0.05);
        float newPitch = (float) (mc.player.getXRot() + secondPitch * speedY * 0.05);

        float gcd1 = getSensitivityGCD();
        float gcd2 = gcd1;

        newYaw = Math.round(newYaw / gcd2) * gcd1;
        newPitch = Math.round(newPitch / gcd1) * gcd2;

        mc.player.setYRot(newYaw);
        mc.player.setXRot(newPitch);
    }

    private Entity getClosestTarget(float range) {
        return PlayerUtil.getTargets(false, false, ignoreTeammates.getProperty(), false, thoughWalls.getProperty(), range).stream()
                .filter(p -> p instanceof Player && mc.screen == null)
                .min(Comparator.comparingDouble(mc.player::distanceToSqr))
                .orElse(null);
    }

    private BodyPart getClosestBodyPartToCrosshair(Entity entity) {
        BodyPart best = null;
        float bestDist = Float.MAX_VALUE;
        double bestY = Double.NEGATIVE_INFINITY;

        for (BodyPart part : BodyPart.values()) {
            Vec3 vec = getTargetVec3d(entity, part);
            float[] rot = getRotationsTo(vec);
            float dist = getAngleDifference(rot[0], mc.player.getYRot()) + getAngleDifference(rot[1], mc.player.getXRot());

            if (dist < bestDist || (dist == bestDist && vec.y > bestY)) {
                best = part;
                bestDist = dist;
                bestY = vec.y;
            }
        }

        return best;
    }

    private Vec3 getTargetVec3d(Entity entity, BodyPart part) {
        Vec3 base = entity.position();
        double height = entity.getBbHeight();
        return switch (part) {
            case HEAD -> base.add(0, entity.getEyeHeight(), 0);
            case NECK -> base.add(0, height * 0.85, 0);
            case CHEST -> base.add(0, height * 0.65, 0);
            case LOWER_CHEST -> base.add(0, height * 0.35, 0);
            case LEFT_ARM -> base.add(-0.3, height * 0.6, 0);
            case RIGHT_ARM -> base.add(0.3, height * 0.6, 0);
        };
    }

    private float[] getRotationsTo(Vec3 target) {
        Vec3 eye = mc.player.getEyePosition(1.0f);
        Vec3 delta = target.subtract(eye);

        float yaw = (float) Mth.wrapDegrees(Mth.atan2(delta.z, delta.x) * 180 / Math.PI - 90);
        float pitch = (float) Mth.wrapDegrees(-Mth.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)) * 180 / Math.PI);

        return new float[]{yaw, pitch};
    }

    private float getAngleDifference(float a1, float a2) {
        float diff = (a1 - a2) % 360.0f;
        return Math.abs(diff > 180.0f ? diff - 360.0f : (diff < -180.0f ? diff + 360.0f : diff));
    }

    private float getSensitivityGCD() {
        float sensitivity = mc.options.sensitivity().get().floatValue();
        float f = sensitivity * 0.6f + 0.2f;
        return f * f * f * 1.2f;
    }

}