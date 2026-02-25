package cc.astralis.event.events.impl.game;

import cc.astralis.event.events.callables.EventDual;
import cc.astralis.event.types.EventModes;
import lombok.Getter;
import net.minecraft.world.entity.Entity;

@Getter
public class EntityInteractEvent extends EventDual {
    private final Entity target;

    public EntityInteractEvent(Entity target, EventModes eventMode) {
        super(eventMode);
        this.target = target;
    }
}
