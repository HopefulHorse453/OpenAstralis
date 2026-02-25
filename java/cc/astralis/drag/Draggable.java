package cc.astralis.drag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Draggable {
    private final String name;
    private float x, y, width, height;

    public void drag(double mouseX, double mouseY) {
        setX((float) mouseX);
        setY((float) mouseY);
    }
}
