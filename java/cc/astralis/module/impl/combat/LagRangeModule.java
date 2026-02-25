package cc.astralis.module.impl.combat;

import cc.astralis.Astralis;
import cc.astralis.component.impl.network.BlinkComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.player.PlayerUtil;
import cc.astralis.util.player.RotationUtil;
import cc.astralis.util.render.ChatUtil;
import cc.astralis.util.render.Render3DUtil;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public class LagRangeModule extends Module {
    private final NumberProperty scoutRange = new NumberProperty("Scout Range", 6, 0, 12, 1);

    private final NumberProperty minRange = new NumberProperty("Min Range", 1, 0, 10, 0.1f),
            maxRange = new NumberProperty("Max Range", 1, 0, 10, 0.1f);

    private final NumberProperty duration = new NumberProperty("Duration", 1, 0, 10000, 1);

    private final BooleanProperty ignoreTeammates = new BooleanProperty("Ignore Teammates", false);
    private final BooleanProperty esp = new BooleanProperty("ESP", false);

    private LivingEntity target;
    private final TimeUtil timeUtil = new TimeUtil();
    private VecDeltaCodec position = null;

    public LagRangeModule() {
        super(Category.COMBAT);
        this.registerProperties(scoutRange, minRange, maxRange, duration, ignoreTeammates, esp);
    }

    private boolean isMovingTowardsTarget(LivingEntity target) {
        if (mc.player == null || target == null) return false;

        double motionX = mc.player.getDeltaMovement().x;
        double motionZ = mc.player.getDeltaMovement().z;

        if (Math.sqrt(motionX * motionX + motionZ * motionZ) < 0.1) return false;

        double toTargetX = target.getX() - mc.player.getX();
        double toTargetZ = target.getZ() - mc.player.getZ();

        double motionMagnitude = Math.sqrt(motionX * motionX + motionZ * motionZ);
        double toTargetMagnitude = Math.sqrt(toTargetX * toTargetX + toTargetZ * toTargetZ);

        double normMotionX = motionX / motionMagnitude;
        double normMotionZ = motionZ / motionMagnitude;
        double normToTargetX = toTargetX / toTargetMagnitude;
        double normToTargetZ = toTargetZ / toTargetMagnitude;

        double dotProduct = normMotionX * normToTargetX + normMotionZ * normToTargetZ;
        return dotProduct > 0;
    }

    @Override
    public void onDisable() {
        Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).stopBlinking();
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        this.setSuffix(duration.getProperty().floatValue() + "ms");

        BlinkComponent blinkComponent = Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class);

        LivingEntity newTarget = PlayerUtil.getTargets(false, false, ignoreTeammates.getProperty(), false, false, scoutRange.getProperty().floatValue())
                .getFirst();

        if (timeUtil.finished(duration.getProperty().longValue()) && blinkComponent.isBlinking()) {
            blinkComponent.stopBlinking();
            position = null;
            return;
        }

        if (target != null && (!target.isAlive() || target.isDeadOrDying() || target.getHealth() <= 0)) {
            ChatUtil.printDebug("dead");
            blinkComponent.stopBlinking();
            position = null;
            target = null;
            return;
        }

        target = newTarget;

        if (target == null) {
            blinkComponent.stopBlinking();
            position = null;
            return;
        }

        float[] rotations = RotationUtil.getRotations(target);
        if (getAngleDifference(rotations[0], mc.player.getYRot()) > 100) {
            blinkComponent.stopBlinking();
            position = null;
            return;
        }

        double distance = RotationUtil.getDistanceToEntityBox(target);

        float min = Math.min(minRange.getProperty().floatValue(), maxRange.getProperty().floatValue());
        float max = Math.max(minRange.getProperty().floatValue(), maxRange.getProperty().floatValue());

        if (distance <= max && distance >= min && !blinkComponent.isBlinking() && isMovingTowardsTarget(target)) {
            if (position == null)
                position = new VecDeltaCodec();

            position.setBase(mc.player.position());
            blinkComponent.startBlinking();
            timeUtil.reset();
        }

        if (distance > max) {
            blinkComponent.stopBlinking();
            position = null;
        }
    }

    private float getAngleDifference(float a1, float a2) {
        float diff = (a1 - a2) % 360.0f;
        return Math.abs(diff > 180.0f ? diff - 360.0f : (diff < -180.0f ? diff + 360.0f : diff));
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (target == null || position == null || !esp.getProperty() || mc.options.getCameraType().isFirstPerson())
            return;

        EntityDimensions dimensions = target.getDimensions(target.getPose());
        AABB box = dimensions.makeBoundingBox(position.getBase());

        Render3DUtil.drawBoxESP(event.getMatricies(), box.inflate(0.08), Astralis.getInstance().getFirstColor(), 150);
    }
}
