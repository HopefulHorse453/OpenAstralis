package cc.astralis.module.impl.combat;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.EntityInteractEvent;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.input.ModifyMovementEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.network.PacketUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class WTapModule extends Module {
    private final ModeProperty mode = new ModeProperty("Mode", "W-Tap", "W-Tap", "S-Tap", "Packet");
    private final NumberProperty packets = new NumberProperty("Packets", 2.0f, 2.0f, 10.0f, 1.0f).setVisible(() -> mode.is("Packet"));
    private final NumberProperty maxHurtTime = new NumberProperty("Max Hurt Time", 1.0f, 1.0f, 10.0f, 1.0f);

    private boolean changeBinds = false;

    public WTapModule() {
        super(Category.COMBAT);
        registerProperties(mode, packets, maxHurtTime);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        changeBinds = false;
    }

    @EventTarget
    public void onAttack(EntityInteractEvent event) {
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        if (!target.isAlive() || target.hurtTime >= maxHurtTime.getProperty().intValue()) return;

        LocalPlayer player = mc.player;
        if (player == null) return;

        switch (mode.getProperty()) {
            case "Packet" -> {
                if (player.isSprinting())
                    PacketUtil.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));

                for (int i = 0; i < packets.getProperty().intValue() - 2; i++) {
                    ServerboundPlayerCommandPacket.Action mode = i % 2 == 0
                            ? ServerboundPlayerCommandPacket.Action.START_SPRINTING
                            : ServerboundPlayerCommandPacket.Action.STOP_SPRINTING;
                    PacketUtil.send(new ServerboundPlayerCommandPacket(player, mode));
                }

                if (!player.isSprinting())
                    PacketUtil.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
            }

            case "W-Tap", "S-Tap" -> {
                Vec3 selfPrev = player.xo != 0 || player.zo != 0 ? new Vec3(player.xo, player.yo, player.zo) : player.position();
                Vec3 selfNow = player.position();

                Vec3 targetPrev = target.xo != 0 || target.zo != 0 ? new Vec3(target.xo, target.yo, target.zo) : target.position();
                Vec3 targetNow = target.position();

                double selfDistanceBefore = targetNow.subtract(selfPrev).horizontalDistance();
                double selfDistanceNow = targetNow.subtract(selfNow).horizontalDistance();

                double targetDistanceBefore = selfNow.subtract(targetPrev).horizontalDistance();
                double targetDistanceNow = selfNow.subtract(targetNow).horizontalDistance();

                boolean selfColliding = selfDistanceBefore > selfDistanceNow;
                boolean targetColliding = targetDistanceBefore > targetDistanceNow;

                if (selfColliding && targetColliding)
                    changeBinds = true;
            }
        }
    }

    @EventTarget
    public void onInput(ModifyMovementEvent event) {
        if (!changeBinds) return;

        switch (mode.getProperty()) {
            case "W-Tap" -> {
                event.setMovementForward(0f);
                event.setMovementSideways(0f);
            }
            case "S-Tap" -> {
                event.setMovementForward(event.getMovementForward() * -1f);
                event.setMovementSideways(event.getMovementSideways() * -1f);
            }
        }
    }
}
