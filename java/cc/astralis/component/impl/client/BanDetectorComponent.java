package cc.astralis.component.impl.client;

import cc.astralis.component.Component;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

/**
 * @author Kawase
 * @since 31.08.2025
 */
public class BanDetectorComponent extends Component {
    @EventTarget
    public void onPacket(PacketEvent packetEvent) {
        if (packetEvent.getPacket() instanceof ClientboundSystemChatPacket gameMessageS2CPacket &&
                gameMessageS2CPacket.content().getString().equalsIgnoreCase("An exception occurred in your connection, so you have been routed to limbo!")
        ) {
            ChatUtil.print("Limbo Detected at" + System.currentTimeMillis());
        }
    }
}
