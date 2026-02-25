package cc.astralis.event.events.impl.game;

import cc.astralis.event.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.MoverType;

@Getter
@Setter
@AllArgsConstructor
public class MoveEvent extends EventCancellable {
    private MoverType movementType;
    private double x, y, z;
}
