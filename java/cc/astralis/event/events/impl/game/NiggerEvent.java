package cc.astralis.event.events.impl.game;

import cc.astralis.event.events.Event;
import cc.astralis.event.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NiggerEvent extends EventCancellable {
    private boolean sprint;
}
