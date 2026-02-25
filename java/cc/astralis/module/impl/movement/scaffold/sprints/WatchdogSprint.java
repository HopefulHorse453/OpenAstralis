package cc.astralis.module.impl.movement.scaffold.sprints;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.movement.ScaffoldWalkModule;
import cc.astralis.module.impl.movement.SpeedModule;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.world.phys.Vec3;

public class WatchdogSprint extends SubModule {
    private final ScaffoldWalkModule sc = (ScaffoldWalkModule) getParentClass();
    private final ModeProperty mode = new ModeProperty("Watchdog Sprint Mode", "Jump", "Jump", "Bypass");
    private TimeUtil timeUtil = new TimeUtil();

    private int slowDownTicks = 0;

    private final long Y_OFFSET_SCALED = -10000000000000000L; // -0.01 scaled by 1e18
    final double SCALE = 1e18;

    private final long Y_OFFSET_SCALED_2 = 111111111111111111L; // scaled by 1e18 (approximate)

    public WatchdogSprint(Module parentClass) {
        super(parentClass,"Watchdog");
        registerPropertyToParentClass(mode);
    }

    @Override
    public void onEnable() {
        if (mc.player != null && mc.player.onGround() && !Astralis.getInstance().getModuleManager().getModule(SpeedModule.class).isToggled()) {
            switch (mode.getProperty()) {
                case "Jump":
                    mc.player.jumpFromGround();
                    break;
                case "Bypass":

                    break;
            }
        }

        slowDownTicks = 0;
        sc.firstJump = true;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        timeUtil.reset();

        MoveUtil.stop();
        super.onDisable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (MoveUtil.isGoingDiagonally()) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x * 0.95, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z * 0.95);
        }


        if (Astralis.getInstance().getModuleManager().getModule(SpeedModule.class).isToggled() ||
                mc.options.keyJump.isDown() || !MoveUtil.isMoving()) {
            return;
        }

        //WHat the fuck this is so useless ????? u can make 6.8 bps boost in 5 line for the offset????
        if (sc.sprint.getProperty() && sc.blocksPlaced != 0 && sc.isToggled()) {
            ChatUtil.printDebug("opffset " + mc.player.tickCount);
            if (mc.player.onGround()) {
                double offset = 1e-10 + (Math.random() * (1e-35));
                event.setY(event.getY() + offset);
                final float speed = (MoveUtil.isGoingDiagonally() ? (0.27f) : 0.275f);
                MoveUtil.strafe(MoveUtil.getPerfectValue(MoveUtil.isGoingDiagonally() ? 0.21f : 0.275f, 0.25f, speed));

                Vec3 pos = mc.player.position();
                double offsetf = (double) Y_OFFSET_SCALED / SCALE;
                mc.player.setPos(pos.x, pos.y + offsetf, pos.z);

                double offsetf2 = (double) Y_OFFSET_SCALED_2 / SCALE;
                mc.player.setPos(pos.x, pos.y + offsetf2, pos.z);
                event.setOnGround(false);
            } else if (!MoveUtil.isGoingDiagonally()) {
                MoveUtil.strafe(MoveUtil.getPerfectValue(0.23f, 0.24f, 0.27f));
            }
        }
    }
}
