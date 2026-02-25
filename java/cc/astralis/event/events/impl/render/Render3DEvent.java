package cc.astralis.event.events.impl.render;

import cc.astralis.event.events.Event;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Render3DEvent implements Event {
    private PoseStack matricies;
}
