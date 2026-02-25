package cc.astralis.module.impl.movement.longjump;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MoveEvent;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import astralis.mixin.accessor.network.EntityVelocityUpdateS2CPacketAccessor;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.combat.VelocityModule;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public class WatchdogFireBall extends SubModule {
    private final ModeProperty mode = new ModeProperty("Mode", "Boost", "Boost", "Flat", "Opal", "Chef", "Chef-High");
    private final NumberProperty speed = new NumberProperty("Speed", 1.5f, 0, 10, 0.1f);

    private boolean start;
    private int ticks;


    //holy paste dude and none of these even work do they...


    public WatchdogFireBall(Module parentClass) {
        super(parentClass,"Watchdog Fireball");
        this.registerPropertiesToParentClass(mode, speed);
    }

    @Override
    public void onEnable() {
        ticks = 0;
        if (Astralis.getInstance().getModuleManager().getModule(VelocityModule.class).isToggled()) {
            Astralis.getInstance().getModuleManager().getModule(VelocityModule.class).setToggled(false);
            ChatUtil.print("Velocity module was disabled to prevent conflicts.");
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (!Astralis.getInstance().getModuleManager().getModule(VelocityModule.class).isToggled()) {
            Astralis.getInstance().getModuleManager().getModule(VelocityModule.class).setToggled(true);
        }

        ticks = 0;
        start = false;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player.hurtTime >= 3) {
            start = true;
        }

        if (start) ticks++;

        switch (mode.getProperty()) {
            case "Flat" -> {
                if (ticks > 0 && ticks < 30) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.01, mc.player.getDeltaMovement().z);
                } else if (ticks >= 31) {
                    this.getParentClass().toggle();
                }
            }
            case "Opal" -> {
                if (ticks == 1) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().y + 0.061, mc.player.getDeltaMovement().z);
                } else {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().y + 0.0283, mc.player.getDeltaMovement().z);
                }
            }
            case "Chef" -> {
                if (ticks >= 1 && ticks <= 33) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.7 - ticks * 0.015, mc.player.getDeltaMovement().z);
                } else if (ticks > 33) {
                    this.getParentClass().toggle();
                }
            }
            case "Chef-High" -> {
                if (ticks >= 1 && ticks <= 28) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, (MoveUtil.isMoving() ? 0.85 : 1.25) - ticks * 0.016, mc.player.getDeltaMovement().z);
                } else if (ticks > 28) {
                    this.getParentClass().toggle();
                }
            }
        }

    //    mc.player.setVelocity(mc.player.getVelocity().add(0, 0.075, 0));

        if (mc.player.onGround() && ticks > 5) {
            this.getParentClass().toggle();
        }
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        if (start && MoveUtil.isMoving()) {
            if (mc.player.hurtTime == 9) {
                if (mode.is("Boost") || mode.is("Opal")) {
                    MoveUtil.strafe(event, speed.getProperty().floatValue());
                } else if (mode.is("Flat")) {
                    ChatUtil.print("flat");
                   // MoveUtil.strafe(event, 1.6);
                } else if (mode.getProperty().contains("Chef")) {
                    ChatUtil.print("cjef");
                    MoveUtil.strafe(event, 1.4);
                }
            }

            if (mc.player.hurtTime == 8 && mode.is("Flat")) {
                MoveUtil.strafe(event, 1.6);
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof ClientboundSetEntityMotionPacket velocityPacket) {
            EntityVelocityUpdateS2CPacketAccessor velocityAccessor = (EntityVelocityUpdateS2CPacketAccessor) velocityPacket;

            if (velocityAccessor.getId() != mc.player.getId()) return;
        }
    }
}
