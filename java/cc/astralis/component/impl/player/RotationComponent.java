package cc.astralis.component.impl.player;

import cc.astralis.Astralis;
import cc.astralis.component.Component;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.game.movementcorrection.JumpCorrectionEvent;
import cc.astralis.event.events.impl.game.movementcorrection.YawCorrectionEvent;
import cc.astralis.event.events.impl.input.InputTickEvent;
import cc.astralis.module.impl.client.RotationsModule;
import cc.astralis.module.impl.movement.FlightModule;
import cc.astralis.module.impl.movement.SpeedModule;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.RotationUtil;
import lombok.Getter;
import lombok.Setter;

//todo: remove this or just remake the hole thing
public class RotationComponent extends Component {
    @Setter
    @Getter
    public static float[] rotations, lastRotations, targetRotations, lastServerRotations;
    @Setter
    @Getter
    public static float fakeYaw, fakePitch, randomisedRotationSpeed;
    @Getter
    @Setter
    public static boolean spoofRotations;

    @Getter
    @Setter
    private static float yawSpeed, pitchSpeed;

    @Getter
    @Setter
    private static boolean fixKeyBinds = true;
    public static boolean shouldMoveFix;

    /*
     * run on tick (if u run it on update it might break depends) to work
     */
    public static void setRotations(float yaw, float pitch, float yawSpeed, float pitchSpeed) {
        RotationComponent.targetRotations = new float[]{yaw, pitch};
        RotationComponent.yawSpeed = yawSpeed;
        RotationComponent.pitchSpeed = pitchSpeed;

        activate = true;
        fixRotations();
    }

    public static void setRotations(float[] rotations, float yawSpeed, float pitchSpeed) {
        RotationComponent.targetRotations = rotations;
        RotationComponent.yawSpeed = yawSpeed;
        RotationComponent.pitchSpeed = pitchSpeed;

        activate = true;
        fixRotations();
    }

    @EventTarget
    public void onUpdateEvent(UpdateEvent event) {
        if (!activate || rotations == null || lastRotations == null || targetRotations == null || lastServerRotations == null) {
            rotations = lastRotations = targetRotations = lastServerRotations = new float[]{mc.player.getYRot(), mc.player.getXRot()};
        }

        if (activate) {
            fixRotations();
        }
    }

    @EventTarget
    public void onTickInputEvent(InputTickEvent event) {
        if (!activate || rotations == null || !shouldMoveFix ||
                Astralis.getInstance().getModuleManager().getModule(SpeedModule.class).isToggled() ||
                Astralis.getInstance().getModuleManager().getModule(FlightModule.class).isToggled() || !fixKeyBinds)
            return;

        MoveUtil.correctMovement(event, getYaw());
    }

    @EventTarget
    public void onYawCorrectionEvent(YawCorrectionEvent event) {
        if (!activate || rotations == null || !shouldMoveFix)
            return;

        event.setYaw(getYaw());
    }

    @EventTarget
    public void onJumpCorrection(JumpCorrectionEvent event) {
        if (!activate || rotations == null || !shouldMoveFix)
            return;

        event.setYaw(getYaw());
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        if (activate && rotations != null) {
            normalizeRotations();

            final float yaw = rotations[0];
            final float pitch = rotations[1];

            event.setYaw(yaw);
            event.setPitch(pitch);

            shouldMoveFix = Astralis.getInstance().getModuleManager().getModule(RotationsModule.class).movementCorrection.getProperty();
            lastServerRotations = new float[]{yaw, pitch};

            if (Math.abs((rotations[0] - mc.player.getYRot()) % 360) < 1 && Math.abs((rotations[1] - mc.player.getXRot())) < 1) {
                activate = false;
            }

            lastRotations = rotations;
        } else {
            lastRotations = new float[]{mc.player.getYRot(), mc.player.getXRot()};
        }

        targetRotations = new float[]{mc.player.getYRot(), mc.player.getXRot()};
    }

    public static void fixRotations() {
        if (lastRotations == null || targetRotations == null) {
            return;
        }

        final float lastYaw = lastRotations[0];
        final float lastPitch = lastRotations[1];
        float targetYaw = targetRotations[0];
        float targetPitch = targetRotations[1];

        targetYaw = normalizeYaw(targetYaw, lastYaw);
        targetPitch = normalizePitch(targetPitch, lastPitch);

        if (yawSpeed != 180) {
            targetYaw = RotationUtil.smoothRotation(lastYaw, targetYaw, yawSpeed);
        }

        if (pitchSpeed != 180) {
            targetPitch = RotationUtil.smoothRotation(lastPitch, targetPitch, pitchSpeed);
        }

        float[] fixedRotations = RotationUtil.getFixedRotations(
                new float[]{targetYaw, targetPitch},
                new float[]{lastYaw, lastPitch}
        );

        rotations = new float[]{fixedRotations[0], fixedRotations[1]};
        if (!spoofRotations) {
            fakeYaw = fixedRotations[0];
            fakePitch = fixedRotations[1];
        }
    }

    private static void normalizeRotations() {
        if (rotations == null || lastRotations == null) return;

        float currentYaw = rotations[0];
        float currentPitch = rotations[1];
        float prevYaw = lastRotations[0];
        float prevPitch = lastRotations[1];

        while (currentYaw - prevYaw < -180.0F) {
            currentYaw += 360.0F;
        }
        while (currentYaw - prevYaw >= 180.0F) {
            currentYaw -= 360.0F;
        }

        while (currentPitch - prevPitch < -180.0F) {
            currentPitch += 360.0F;
        }
        while (currentPitch - prevPitch >= 180.0F) {
            currentPitch -= 360.0F;
        }

        rotations[0] = currentYaw;
        rotations[1] = currentPitch;
    }

    private static float normalizeYaw(float targetYaw, float lastYaw) {
        float normalizedYaw = targetYaw;

        while (normalizedYaw - lastYaw < -180.0F) {
            normalizedYaw += 360.0F;
        }
        while (normalizedYaw - lastYaw >= 180.0F) {
            normalizedYaw -= 360.0F;
        }

        return normalizedYaw;
    }

    private static float normalizePitch(float targetPitch, float lastPitch) {
        float normalizedPitch = targetPitch;

        while (normalizedPitch - lastPitch < -180.0F) {
            normalizedPitch += 360.0F;
        }
        while (normalizedPitch - lastPitch >= 180.0F) {
            normalizedPitch -= 360.0F;
        }

        return normalizedPitch;
    }

    public static float getYaw() {
        return rotations[0];
    }

    public static float getPitch() {
        return rotations[1];
    }
}
