package cc.astralis.ui.screens.clickgui.astralis;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Component {
    private float x, y, width, height;

    public void render(final float x, final float y, final float mouseX, final float mouseY) { /* w */ }
    public void mouseClicked(final double mouseX, final double mouseY, final int button) { /* w */ }
    public void mouseReleased(final double mouseX, final double mouseY, final int button) { /* w */ }
    public void mouseScrolled(final double amount) { /* w */ }
    public void shader() { /* w */ };
}
