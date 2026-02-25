package cc.astralis.event.events.impl.game;

import cc.astralis.event.events.callables.EventDual;
import cc.astralis.event.types.EventModes;

public class TickEvent extends EventDual {
    public TickEvent(EventModes eventModes) {
        super(eventModes);
    }
}
