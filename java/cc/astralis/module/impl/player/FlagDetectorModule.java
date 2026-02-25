package cc.astralis.module.impl.player;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

public class FlagDetectorModule extends Module {
    public FlagDetectorModule() {
        super(Category.PLAYER);
    }

    @EventTarget
    public void onPacket(PacketEvent event)  {
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            ChatUtil.print("Flag detected!");
        }
    }
}
