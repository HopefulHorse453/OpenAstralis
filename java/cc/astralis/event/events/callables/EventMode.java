package cc.astralis.event.events.callables;

import cc.astralis.event.events.Event;
import cc.astralis.event.types.EventModes;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EventMode implements Event {
    public EventModes eventType;
}
