package cc.astralis.module.impl.movement.speed;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.event.events.impl.game.StrafeEvent;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.game.movementcorrection.YawCorrectionEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.movement.ScaffoldRecodeModule;
import cc.astralis.module.impl.movement.ScaffoldWalkModule;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.math.MathUtil;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.PlayerUtil;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WatchdogSpeed extends SubModule {
    private final ModeProperty eventMode = new ModeProperty("Event Mode", "Strafe", "Strafe", "Update", "Correction");
    private final NumberProperty groundSpeedNoSpeedPot = new NumberProperty("Ground Speed (No Speed Pot)", 0.45f, 0, 2, 0.01f),
            groundSpeedSpeedOne = new NumberProperty("Ground Speed (Speed One)", 0.45f, 0, 2, 0.01f),
            groundSpeedSpeedTwo = new NumberProperty("Ground Speed (Speed Two)", 0.45f, 0, 2, 0.01f);
    private final BooleanProperty airStrafe = new BooleanProperty("Air Strafe", true);
    private final BooleanProperty airStrafeExtra = new BooleanProperty("Air Strafe Extra", true);

    private final NumberProperty airStrafeTolerance = new NumberProperty("Air Strafe Tolerance", 80, 50, 90, 5);
    private final BooleanProperty watchdogStrafe = new BooleanProperty("Watchdog Strafe", true),
            watchdogLowHop = new BooleanProperty("Watchdog Low Hop", true),
            sevenTick = new BooleanProperty("Seven Tick", true);
    private final BooleanProperty experimentalStrafeCheck = new BooleanProperty("Experimental Strafe Chech", true);

    private boolean up = false;
    private boolean strafe, down;
    private boolean didFireBallDMG;
    private final TimeUtil timeUtil = new TimeUtil();

    public WatchdogSpeed(Module parentClass) {
        super(parentClass,"Watchdog");
        this.registerPropertiesToParentClass(eventMode,
                groundSpeedNoSpeedPot, groundSpeedSpeedOne, groundSpeedSpeedTwo,
                airStrafe, airStrafeTolerance.setVisible(airStrafe::getProperty),
                watchdogStrafe,
                watchdogLowHop, sevenTick, airStrafeExtra.setVisible(airStrafe::getProperty), experimentalStrafeCheck
        );
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            didFireBallDMG = false;
            up = false;
            //Astralis.getInstance().getModuleManager().getModule(ScaffoldWalkModule.class).startY = mc.player.getBlockY();
        }
        super.onEnable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        //how many client is this pasted from raven looking ahhh values
        // stfu they are mine retard
        if ((Astralis.getInstance().getModuleManager().getModule(ScaffoldRecodeModule.class).isToggled() && mc.options.keyJump.isDown()))
            return;

        Block underBlock = mc.level.getBlockState(mc.player.blockPosition().below()).getBlock();

        if (underBlock.getDescriptionId().contains("stairs") || underBlock.getDescriptionId().contains("slab"))
            timeUtil.reset();

        if (mc.player.onGround() && getParentClass().isToggled()) {
            strafe = down = false;
            mc.player.jumpFromGround();

            if (timeUtil.finished(200)) {
                MoveUtil.strafe(MoveUtil.getPerfectValue(
                        groundSpeedNoSpeedPot.getProperty().floatValue(),
                        groundSpeedSpeedOne.getProperty().floatValue(),
                        groundSpeedSpeedTwo.getProperty().floatValue())
                );
            }
        }

        airStrafe();

        if (mc.player.horizontalCollision && !MoveUtil.isMoving())
            return;

        if (watchdogLowHop.getProperty() ) {
            if (!sevenTick.getProperty()) {
                int simpleY = (int) Math.round((mc.player.getY() % 1) * 10000);


                switch (simpleY) {
                    case 13 -> PlayerUtil.setMotionY(PlayerUtil.getMotionY() - 0.02483);
                    case 2000 -> PlayerUtil.setMotionY(PlayerUtil.getMotionY() - 0.1913);
                }

                if (simpleY == 13) {
                    down = true;
                }

                if (down) {
                    event.setY(event.getY() - 1E-5);
                }

                if (simpleY == 3426) strafe = true;

                if (strafe) {
                    MoveUtil.strafe(MoveUtil.getBaseSpeed());
                }
            } else {

            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (airStrafeExtra.getProperty())
            airStrafe();

        if (eventMode.is("Update"))
            strafe();

        if (sevenTick.getProperty() && watchdogLowHop.getProperty()) {
            PlayerUtil.setMotionY(PlayerUtil.getMotionY() + switch (offGroundTicks) {
                case 1 -> 0.05;
                case 2 -> 0.012;
                case 3 -> mc.player.hurtTime != 0 ? -0.13 : -0.135 /* -0.136 */;
                case 4 -> -0.2;
                default -> 0;
            });
        }
    }

    @EventTarget
    public void onYawCorrection(YawCorrectionEvent event) {
        if (airStrafeExtra.getProperty())
            airStrafe();

        if (eventMode.is("Correction"))
            strafe();
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (airStrafeExtra.getProperty())
            airStrafe();

        if (eventMode.is("Strafe"))
            strafe();
    }

    @EventTarget
    public void onPacket(PacketEvent event)  {
        if (event.getPacket() instanceof ClientboundExplodePacket && !didFireBallDMG) {
            didFireBallDMG = true;
            timeUtil.reset();
        }
    }

    private void strafe() {
        if (!watchdogStrafe.getProperty())
            return;

        if (mc.player.onGround()) {
            up = false;
        } else {

            if (offGroundTicks == 1) {
                double speedWithSpeed = MoveUtil.getPerfectValue(0.33f, 0.4f, 0.48f);
                MoveUtil.strafe(Math.max(speedWithSpeed, MoveUtil.getSpeed()));
               // ChatUtil.print("strafe " + offGroundTicks);
            }

            double distanceToGround = PlayerUtil.getDistanceToGround();

            double roundedDistance = MathUtil.roundToDecimalPlaces(distanceToGround, 2);
            if ((roundedDistance < 0 || (roundedDistance > (watchdogLowHop.getProperty() ? 1.2 : 1.3) && !Astralis.getInstance().getModuleManager().getModule(ScaffoldWalkModule.class).isToggled())) && experimentalStrafeCheck.getProperty()) {
              //  ChatUtil.print("return exp  " + roundedDistance);
                return;
            }

            if (isFullBlock(
                    mc.player.getX(),
                    mc.player.getY() + mc.player.getDeltaMovement().y,
                    mc.player.getZ()
            ) && offGroundTicks > 2) {
                MoveUtil.strafe();
              //  ChatUtil.print("strafe " + offGroundTicks);
            }

            if (offGroundTicks >= 2 && (
                    isFullBlock(
                            mc.player.getX(),
                            mc.player.getY() + mc.player.getDeltaMovement().y * 3,
                            mc.player.getZ()
                    ) || offGroundTicks == 9
            )) {
                if ((distanceToGround < 0 || distanceToGround >= 1) && !experimentalStrafeCheck.getProperty() && !Astralis.getInstance().getModuleManager().getModule(ScaffoldRecodeModule.class).isToggled()) {
                    ChatUtil.printDebug("return dist " + distanceToGround);
                }

              //  ChatUtil.print("strafe " + offGroundTicks);

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

    private void airStrafe() {
        if (!airStrafe.getProperty())
            return;

        if (!mc.player.onGround() && mc.player.hurtTime < 3 && (mc.player.getDeltaMovement().x != 0 || mc.player.getDeltaMovement().z != 0)) {
            float moveDir = MoveUtil.getRawDirection();
            float currentMotionDir = strafeDirection();
            float diff = airStrafeExtra.getProperty() ? Math.abs(((moveDir - currentMotionDir + 540) % 360) - 180) : Math.abs(moveDir - currentMotionDir);
            int range = airStrafeTolerance.getProperty().intValue();

            if (airStrafeExtra.getProperty() ? (diff >= 180 - range && diff <= 180 + range) : diff > 180 - range && diff < 180 + range) {
                mc.player.setDeltaMovement(
                        -mc.player.getDeltaMovement().x * 0.85,
                        mc.player.getDeltaMovement().y,
                        -mc.player.getDeltaMovement().z * 0.85
                );
            }
        }
    }

    private float strafeDirection() {
        float yaw = (float) Math.toDegrees(Math.atan2(-mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().z));
        if (yaw < 0) yaw += 360;
        return yaw;
    }

    private boolean isFullBlock(double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockState state = mc.level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(mc.level, pos);

        return shape == Shapes.block();
    }
}
