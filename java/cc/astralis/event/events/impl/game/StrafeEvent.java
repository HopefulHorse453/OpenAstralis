package cc.astralis.event.events.impl.game;

import cc.astralis.event.events.callables.EventCancellable;
import cc.astralis.event.events.callables.EventDual;
import cc.astralis.event.types.EventModes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
public class StrafeEvent extends EventDual {
    private final Vec3 type;

    public StrafeEvent(Vec3 type, EventModes eventModes) {
        super(eventModes);
        this.type = type;
    }
}
