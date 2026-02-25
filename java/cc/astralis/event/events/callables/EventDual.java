package cc.astralis.event.events.callables;

import cc.astralis.event.types.EventModes;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class EventDual extends EventCancellable {
    public EventModes eventMode;
}
