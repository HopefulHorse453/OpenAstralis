package cc.astralis.module.impl.player;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.PlayerUtil;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class AntiVoidModule extends Module {
    private final ModeProperty mode = new ModeProperty("Mode", "Blink", "Blink", "Stop");
    private double lastGroundY, lastGroundX, lastGroundZ;
    private boolean wasOverVoid;
    private int voidTicks;

    public AntiVoidModule() {
        super(Category.PLAYER);
        registerProperties(mode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null && mc.player.onGround()) {
            lastGroundX = mc.player.getX();
            lastGroundY = mc.player.getY();
            lastGroundZ = mc.player.getZ();
        }
        voidTicks = 0;
        wasOverVoid = false;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if(mode.is("Stop") && PlayerUtil.isPlayerOverVoid() && !mc.player.onGround()) {
          event.setCancelled(true);
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if(mode.getProperty().equals("Blink"))
        if (event.getEventMode() == EventModes.RECEIVE) return;

        if (event.getPacket() instanceof ServerboundMovePlayerPacket) {

            boolean overVoid = PlayerUtil.isPlayerOverVoid();

            if (overVoid) {
                if (mc.player.fallDistance < 5) {
                    event.setCancelled(true);
                    wasOverVoid = true;
                } else if (wasOverVoid) {
                    MoveUtil.stop();
                    mc.player.setPos(lastGroundX, lastGroundY, lastGroundZ);
                    mc.player.fallDistance = 0;
                    wasOverVoid = false;
                    ChatUtil.print("Saved from void!");
                }
            } else {
                if (mc.player.tickCount % 10 == 0 && mc.player.onGround()) {
                    lastGroundX = mc.player.getX();
                    lastGroundY = mc.player.getY();
                    lastGroundZ = mc.player.getZ();
                    wasOverVoid = false;
                }
            }
        }
    }
}