package cc.astralis.module.impl.player;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.ModeProperty;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

public class NoRotateModule extends Module {
    private final ModeProperty mode = new ModeProperty("Mode", "Cancel", "Cancel", "Adjust");

    public NoRotateModule() {
        super(Category.EXPLOIT);
        registerProperties(mode);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
       this.setSuffix(mode.getProperty());
    }

    @EventTarget
    public void onPacket(PacketEvent event){
        if(event.getEventMode() == EventModes.RECEIVE && event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            switch (mode.getProperty()){
                case "Cancel":
                    event.setCancelled(true);
                    break;
                case "Adjust":
                    break;
            }
        }
    }
}
