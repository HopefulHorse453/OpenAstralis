package cc.astralis.event.events.impl.game;

import cc.astralis.event.events.callables.EventCancellable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlowDownEvent extends EventCancellable {
    public float slowDown;
}
