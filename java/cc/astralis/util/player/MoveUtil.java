 package cc.astralis.util.player;

import cc.astralis.event.events.impl.game.MoveEvent;
import cc.astralis.event.events.impl.input.InputTickEvent;
import cc.astralis.interfaces.IAccess;
import cc.astralis.util.Data;
import io.github.kawase.NativeObfuscate;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec2;

 public class MoveUtil implements IAccess {
    public static final float WALK_SPEED = .221f,
            SWIM_SPEED = .115f / WALK_SPEED,
            SNEAK_SPEED = .3f,
            SPRINTING_SPEED = 1.3f;

    public static float getDirection() {
        return (float) Math.toRadians(getDirectionBasedOnYaw(mc.player.getYRot()));
    }

    public static float getRawDirection() {
        return getDirectionBasedOnYaw(mc.player.getYRot());
    }

    public static float getRawDirectionNoKeys() {
        return getDirectionBasedOnYaw(mc.player.getYRot(), false);
    }

    public static float getDirectionBasedOnYaw(final float inputYaw) {
        return getDirectionBasedOnYaw(inputYaw, true);
    }

     public static float getDirectionBasedOnYaw(final float inputYaw, boolean keys) {
         Vec2 movement = mc.player.input.getMoveVector();

         float yaw = inputYaw;
         float forward = 1F;

         if (movement.y < 0F) {
             yaw += 180F;
         }

         if (movement.y < 0F) {
             forward = -0.5F;
         } else if (movement.y > 0F) {
             forward = 0.5F;
         }

         if (keys) {
             if (movement.x > 0F) {
                 yaw -= 90F * forward;
             }
             if (movement.x < 0F) {
                 yaw += 90F * forward;
             }
         }

         return yaw;
     }

    public static void strafe() {
        strafe(MoveUtil.getSpeed());
    }

    public static void strafe(double speed) {
        if (isMoving()) {
            mc.player.setDeltaMovement(-Math.sin(getDirection()) * speed, mc.player.getDeltaMovement().y, Math.cos(getDirection()) * speed);
        } else {
            stop();
        }
    }

    public static void strafe(MoveEvent event, double speed) {
        if (isMoving()) {
            event.setX(-Math.sin(getDirection()) * speed);
            event.setZ(Math.cos(getDirection()) * speed);
        } else {
            stop();
        }
    }

    public static boolean isGoingDiagonally() {
        return (Math.abs(mc.player.getDeltaMovement().x) > 0.05 && Math.abs(mc.player.getDeltaMovement().z) > 0.05);
    }

    public static double getBaseSpeed() {
        boolean useModifiers = false;
        double speed;

        //if (mc.player.isInWeb()) {
        //            speed = WEB_SPEED * WALK_SPEED;
        //        } else
        if (mc.player.isInWater() || mc.player.isInLava()) {
            speed = SWIM_SPEED * WALK_SPEED;
            //  int level = EnchantmentHelper.getDepthStrider(player);
            //            if (level > 0) {
            //                speed *= DEPTH_STRIDER[MathHelper.clamp(level, 0, 3)];
            //                useModifiers = true;
            //            }
        } else if (mc.player.isShiftKeyDown()) {
            speed = SNEAK_SPEED * WALK_SPEED;
        } else {
            speed = WALK_SPEED;
            useModifiers = true;
        }

        if (useModifiers) {
            if (mc.player.isSprinting()) {
                speed *= SPRINTING_SPEED;
            }

            if (mc.player.hasEffect(MobEffects.SPEED)) {
                speed *= 1 + (0.2 * (mc.player.getEffect(MobEffects.SPEED).getAmplifier() + 1));
            }

            if (mc.player.hasEffect(MobEffects.SLOWNESS)) {
                speed = 0.29f;
            }
        }

        return speed;
    }

    public static float getPerfectValue(float noSpeed, float speed1, float speed2) {
        float speed = noSpeed;

        for (MobEffectInstance potionEffect : mc.player.getActiveEffects()) {
            if (potionEffect.getEffect() == MobEffects.SPEED) {
                speed = switch (potionEffect.getAmplifier()) {
                    case 1 -> speed2;
                    case 0 -> speed1;
                    default -> 0;
                };
            }
        }

        if (!mc.player.hasEffect(MobEffects.SPEED))
            speed = noSpeed;

        return speed;
    }

    public static void stop() {
        mc.player.setDeltaMovement(0, mc.player.getDeltaMovement().y, 0);
    }

     public static boolean isMoving() {
         Vec2 movement = mc.player.input.getMoveVector();
         return movement.x != 0 || movement.y != 0;
     }

     public static boolean enoughMovementForSprinting() {
        Vec2 movement = mc.player.input.getMoveVector();
        return Math.abs(movement.y) >= .8f || Math.abs(movement.y) >= .8f;
    }

    public static double predictedMotion(final double motion, final int ticks) {
        if (ticks == 0) return motion;
        double predicted = motion;

        for (int i = 0; i < ticks; i++) {
            predicted = (predicted - 0.08) * 0.98F;
        }

        return predicted;
    }

    public static double getSpeed() {
        return Math.hypot(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().z);
    }

    public static void correctMovement(InputTickEvent event, float yaw) {
        boolean up = event.up, down = event.down, left = event.left, right = event.right;

        if (mc.player != null && (up != down || left != right)) {
            float y = Mth.wrapDegrees(mc.player.getYRot() + (up == down ? left ? -90 : 90 : up ? left == right ? 0 : left ? -45 : 45 : left == right ? 180 : left ? -135 : 135) - yaw);
            up = false;
            down = false;
            left = false;
            right = false;
            if (y >= -22.5F && y < 22.5F) up = true;
            if (y < -22.5F && y >= -67.5F) {
                left = true;
                up = true;
            }
            if (y < -67.5F && y >= -112.5F) left = true;
            if (y < -112.5F && y >= -157.5F) {
                left = true;
                down = true;
            }
            if (y < -157.5F && y >= -180 || y >= 157.5F && y < 180) down = true;
            if (y >= 112.5F && y < 157.5F) {
                right = true;
                down = true;
            }
            if (y >= 67.5F && y < 112.5F) right = true;
            if (y >= 22.5F && y < 67.5F) {
                right = true;
                up = true;
            }
        }

        event.up = up;
        event.down = down;
        event.left = left;
        event.right = right;
    }

    public static double getDirection(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public static double getBPS() {
        double bps = (Math.hypot(mc.player.getX() - mc.player.xo, mc.player.getZ() - mc.player.zo) * Data.timer) * 20;
        return Math.round(bps * 100.0) / 100.0;
    }

     public static boolean isPressingForwardAndStrafe() {
         boolean forward = mc.options.keyUp.isDown();
         boolean left = mc.options.keyLeft.isDown();
         boolean right = mc.options.keyRight.isDown();

         return forward && (left || right);
     }

     public static float getMovementYaw(LocalPlayer player) {
         float yaw = player.getYRot();

         boolean forward = mc.options.keyUp.isDown();
         boolean back = mc.options.keyDown.isDown();
         boolean left = mc.options.keyLeft.isDown();
         boolean right = mc.options.keyRight.isDown();

         int forwardVal = forward ? 1 : back ? -1 : 0;
         int strafeVal = right ? 1 : left ? -1 : 0;

         if (forwardVal == 0 && strafeVal == 0) return yaw;

         double angle = Math.atan2(strafeVal, forwardVal);
         yaw += (float) Math.toDegrees(angle);

         return yaw;
     }

 }

