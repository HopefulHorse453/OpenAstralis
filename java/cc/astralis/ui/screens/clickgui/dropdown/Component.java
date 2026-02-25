package cc.astralis.ui.screens.clickgui.dropdown;

import cc.astralis.interfaces.Fonts;
import cc.astralis.interfaces.IAccess;
import cc.astralis.property.Property;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Component implements IAccess, Fonts {
    private float width, height;
    private float x, y;
    private Property<?> property;

    public void render(float mouseX, float mouseY) { /* w */ }

    public void release(double mouseX, double mouseY, int button) { /* w */ }
    public void click(double mouseX, double mouseY, int button) { /* w */ }
}
