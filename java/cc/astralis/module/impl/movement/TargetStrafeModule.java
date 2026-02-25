package cc.astralis.module.impl.movement;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MoveEvent;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.event.types.Priority;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.combat.KillauraModule;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.math.MathUtil;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.RotationUtil;
import cc.astralis.util.render.Render3DUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class TargetStrafeModule extends Module {
    private final NumberProperty range = new NumberProperty("Range", 3, 0, 10, 0.1f);

    public TargetStrafeModule() {
        super(Category.MOVEMENT);
        registerProperties(range);
    }

    private int strafeDirection = 1;

    @EventTarget(Priority.LOWEST)
    public void onUpdate(MoveEvent event) {
        this.setSuffix(String.valueOf(MathUtil.roundToDecimalPlaces(range.getProperty().intValue(), 6)));

        LivingEntity target = Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).target;
        final SpeedModule speedModule = Astralis.getInstance().getModuleManager().getModule(SpeedModule.class);
        final FlightModule flightModule = Astralis.getInstance().getModuleManager().getModule(FlightModule.class);

        if (target == null || !MoveUtil.isMoving() || mc.player.distanceTo(target) > 3 ||
                (!speedModule.isToggled() && !flightModule.isToggled())) {
            return;
        }

        double speed = MoveUtil.getSpeed();
        float rotation = RotationUtil.getRotations(target)[0];

        Vec3 strafeVec = new Vec3(-Math.sin(Math.toRadians(mc.player.getYRot())) * strafeDirection,
                0,
                Math.cos(Math.toRadians(mc.player.getYRot())) * strafeDirection);
        Vec3 nextPos = mc.player.position().add(strafeVec.scale(speed));

        if (!mc.level.getBlockState(new BlockPos(
                (int) nextPos.x,
                (int) nextPos.y,
                (int) nextPos.z)).isAir() || mc.player.horizontalCollision
        ) {
            strafeDirection = -strafeDirection;
        }

        setSpeed(event, speed, rotation, strafeDirection,
                mc.player.distanceTo(target) <= this.range.getProperty().floatValue() ? 0 : 1.0);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        LivingEntity target = Astralis.getInstance().getModuleManager().getModule(KillauraModule.class).target;
        if (target == null) return;

        Render3DUtil.drawCircle(event.getMatricies(), target, range.getProperty().floatValue());
    }

    public static void setSpeed(MoveEvent moveEvent, double moveSpeed, float yaw, double strafe, double forward) {
        if (forward != 0.0D) {
            yaw += strafe > 0.0D ? (forward > 0.0D ? -45 : 45) : (strafe < 0.0D ? (forward > 0.0D ? 45 : -45) : 0);
            strafe = 0.0D;
            forward = Math.signum(forward); // Sets forward to 1.0D, -1.0D, or 0.0D
        }

        strafe = Math.signum(strafe); // Sets strafe to 1.0D, -1.0D, or 0.0D

        double rad = Math.toRadians(yaw + 90.0F);
        double mx = Math.cos(rad);
        double mz = Math.sin(rad);

        moveEvent.setX(forward * moveSpeed * mx + strafe * moveSpeed * mz);
        moveEvent.setZ(forward * moveSpeed * mz - strafe * moveSpeed * mx);
    }
}
