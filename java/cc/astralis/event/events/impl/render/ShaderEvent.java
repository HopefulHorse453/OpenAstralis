package cc.astralis.event.events.impl.render;

import cc.astralis.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ShaderEvent implements Event {
    private final float width, height;
}
