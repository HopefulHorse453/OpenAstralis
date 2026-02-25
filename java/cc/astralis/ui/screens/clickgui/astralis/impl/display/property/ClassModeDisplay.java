package cc.astralis.ui.screens.clickgui.astralis.impl.display.property;

import cc.astralis.font.UIFont;
import cc.astralis.font.FontManager;
import cc.astralis.module.SubModule;
import cc.astralis.property.properties.ClassModeProperty;
import cc.astralis.ui.screens.clickgui.astralis.Component;
import cc.astralis.ui.screens.clickgui.astralis.impl.display.ModuleDisplay;
import cc.astralis.util.render.RenderUtil;

import java.util.ArrayList;
import java.util.List;

public class ClassModeDisplay extends Component {
    private final ClassModeProperty property;

    public ClassModeDisplay(final ClassModeProperty property, final float width, final float height) {
        this.property = property;
        setWidth(width);
        setHeight(height);
    }

    @Override
    public void render(final float x, final float y, final float mouseX, final float mouseY) {
        setX(x);
        setY(y);
        final UIFont UIFont = FontManager.getFont("Sf-Ui", 10);
        UIFont.drawStringWithShadow(property.getName(), x + ModuleDisplay.PADDING, y + ModuleDisplay.PROPERTY_PADDING, ModuleDisplay.DESCRIPTION);
        UIFont.drawStringWithShadow(property.getProperty().getFormatedName(), x + getWidth() - (UIFont.getStringWidth(property.getProperty().getFormatedName()) + ModuleDisplay.PADDING), y + ModuleDisplay.PROPERTY_PADDING, ModuleDisplay.DESCRIPTION);
    }

    @Override
    public void mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            SubModule currentMode = property.getProperty();
            List<SubModule> modes = new ArrayList<>(property.getClassModes().values());
            int currentIndex = modes.indexOf(currentMode);
            int nextIndex = (currentIndex + 1) % modes.size();
            SubModule nextMode = modes.get(nextIndex);

            if (currentMode != null) currentMode.setSelected(false);
            nextMode.setSelected(true);
            property.setProperty(nextMode);
        }
    }
}
