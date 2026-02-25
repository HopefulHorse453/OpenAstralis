package cc.astralis.component.impl.network;

import cc.astralis.component.Component;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.util.network.PacketUtil;
import cc.astralis.util.render.ChatUtil;
import lombok.Getter;
import net.minecraft.network.protocol.Packet;
import java.util.LinkedList;
import java.util.Queue;

public class BlinkComponent extends Component {
    private final Queue<Packet<?>> packets = new LinkedList<>();
    @Getter
    private boolean blinking = false;

    public void startBlinking() {
        if (blinking)
            return;

        ChatUtil.printDebug("§b[blink] started");
        packets.clear();
        blinking = true;
        onEnable();
    }

    public void stopBlinking() {
        if (!blinking)
            return;

        ChatUtil.printDebug("§b[blink] stopped");
        blinking = false;
        onDisable();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!blinking || event.getEventMode() == EventModes.RECEIVE || mc.level == null) {
            return;
        }

        event.setCancelled(true);
        packets.add(event.getPacket());
    }

    @Override
    public void onEnable() {
        if (blinking) {
            super.onEnable();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        while (!packets.isEmpty()) {
            PacketUtil.send(packets.poll());
        }
    }
}
