package cc.astralis.module.impl.player;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.input.InputTickEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.ModeProperty;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class AirStuckModule extends Module {
    private final ModeProperty mode = new ModeProperty("Mode", "Cancel", "Cancel", "Stop", "Stop 2");
    public AirStuckModule() {
        super(Category.EXPLOIT);
        registerProperty(mode);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        switch (mode.getProperty()) {
            case "Cancel" -> event.setCancelled(true);
            case "Stop", "Stop 2" -> mc.player.setDeltaMovement(0, 0, 0);
        }
    }

    @EventTarget
    public void onInput(InputTickEvent event) {
        if (mode.is("Stop")) {
            event.sprint = false;
            event.jump = false;
            event.shift = false;
            event.left = false;
            event.right = false;
            event.up = false;
            event.down = false;
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mode.is("Stop"))
            event.setCancelled(true);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getEventMode() == EventModes.RECEIVE || mc.level == null) {
            return;
        }

        if (mode.is("Stop 2") && event.getPacket() instanceof ServerboundMovePlayerPacket)
            event.setCancelled(true);
    }
}
