package cc.astralis.event.events.impl.game;

import cc.astralis.event.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MotionEvent extends EventCancellable {
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;
}
