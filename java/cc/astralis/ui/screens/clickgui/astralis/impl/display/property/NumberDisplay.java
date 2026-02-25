package cc.astralis.ui.screens.clickgui.astralis.impl.display.property;

import cc.astralis.Astralis;
import cc.astralis.font.UIFont;
import cc.astralis.font.FontManager;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.ui.screens.clickgui.astralis.Component;
import cc.astralis.ui.screens.clickgui.astralis.impl.display.ModuleDisplay;
import cc.astralis.util.math.MathUtil;
import cc.astralis.util.render.RenderUtil;
import cc.astralis.skija.utils.SkijaUtil;
import imgui.type.ImFloat;

import java.awt.*;

public class NumberDisplay extends Component {
    private final NumberProperty property;
    private boolean dragging;

    private static final int SLIDER_HEIGHT = 7;
    private static final Color START = new Color(40, 40, 40);

    public NumberDisplay(final NumberProperty property, final float width, final float height) {
        this.property = property;
        setWidth(width);
        setHeight(height);
    }

    @Override
    public void render(final float x, final float y, final float mouseX, final float mouseY) {
        setX(x);
        setY(y);
        final UIFont uiFont = FontManager.getFont("Sf-Ui", 10);

        uiFont.drawStringWithShadow(property.getName(), x + ModuleDisplay.PADDING, y + ModuleDisplay.PROPERTY_PADDING, ModuleDisplay.DESCRIPTION);

        final String value = String.valueOf(property.getProperty());
        uiFont.drawStringWithShadow(value, x + getWidth() - (uiFont.getStringWidth(value) + ModuleDisplay.PADDING), y + ModuleDisplay.PROPERTY_PADDING, ModuleDisplay.DESCRIPTION);

        final float width = getWidth() - (ModuleDisplay.PADDING * 2);
        float widthPercentage = ((property.getProperty().floatValue()) - property.getMin()) / (property.getMax() - property.getMin());


        if (dragging) {
            float percent = Math.min(1, Math.max(0, (mouseX - (x + ModuleDisplay.PADDING)) / width));
            double propertyValue = MathUtil.interpolate(property.getMin(), property.getMax(), percent);
            propertyValue = snapToIncrement(propertyValue, property.getIncrement());
            property.setProperty(new ImFloat((float) propertyValue));
        }
        SkijaUtil.roundedRectangle(x + ModuleDisplay.PADDING, y + super.getHeight(), width, SLIDER_HEIGHT, 2, START);
        SkijaUtil.roundedRectangleGradient(x + ModuleDisplay.PADDING, y + super.getHeight(), width * widthPercentage, SLIDER_HEIGHT, 2,
                Astralis.getInstance().getFirstColor(),
                Astralis.getInstance().getSecondColor(),
                false
        );
    }

    @Override
    public void mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(final double mouseX, final double mouseY, final int button) {
        dragging = false;
    }

    @Override
    public float getHeight() {
        return super.getHeight() + ModuleDisplay.PROPERTY_PADDING + SLIDER_HEIGHT;
    }

    private double snapToIncrement(double value, float increment) {
        if (increment == 0) return value;
        // we round it again to a decimal place bc it sometimes adds into 0.0000001 idk why so doing this fixes it.
        return MathUtil.roundToDecimalPlaces(Math.round(value / increment) * increment, 6 /* should be alr */);
    }
}
