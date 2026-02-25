package cc.astralis.event.events.impl.network;

import cc.astralis.event.events.callables.EventDual;
import cc.astralis.event.types.EventModes;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.protocol.Packet;

@Getter
@Setter
public class PacketEvent extends EventDual {
    private Packet<?> packet;

    public PacketEvent(Packet<?> packet, EventModes eventModes) {
        super(eventModes);
        this.packet = packet;
    }
}
