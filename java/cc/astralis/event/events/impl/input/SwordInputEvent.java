package cc.astralis.event.events.impl.input;

import cc.astralis.event.events.Event;
import cc.astralis.event.events.callables.EventCancellable;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Kawase
 * @since 12.10.2025
 */
@Getter
@Setter
public class SwordInputEvent implements Event {
    private boolean isBlocking = false;
}
