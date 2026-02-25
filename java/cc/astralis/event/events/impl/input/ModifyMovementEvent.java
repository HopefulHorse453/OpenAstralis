package cc.astralis.event.events.impl.input;

import cc.astralis.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModifyMovementEvent implements Event {
    private float movementSideways, movementForward;

    public ModifyMovementEvent(float movementSideways, float movementForward) {
        this.movementSideways = movementSideways;
        this.movementForward = movementForward;
    }
}
