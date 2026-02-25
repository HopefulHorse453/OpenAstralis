package cc.astralis.event.events.impl.game;

import cc.astralis.event.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Kawase
 * @since 29.10.2025
 */
@Getter
@Setter
@AllArgsConstructor
public class VelocityUpdateEvent extends EventCancellable {
    private double velocityX, velocityY, velocityZ;
    private boolean explosion;
}
