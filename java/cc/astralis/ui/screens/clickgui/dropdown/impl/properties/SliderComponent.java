package cc.astralis.ui.screens.clickgui.dropdown.impl.properties;

import cc.astralis.font.FontManager;
import cc.astralis.font.UIFont;
import cc.astralis.skija.utils.SkijaUtil;
import imgui.type.ImFloat;
import cc.astralis.Astralis;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.ui.screens.clickgui.dropdown.Component;
import cc.astralis.util.math.MathUtil;
import cc.astralis.util.render.RenderUtil;

import java.awt.*;

public class SliderComponent extends Component {
    private final NumberProperty numberProperty;
    private boolean dragging;

    public SliderComponent(NumberProperty numberProperty) {
        this.numberProperty = numberProperty;
        this.setProperty(numberProperty);
        this.setWidth(110);
        this.setHeight(15);
    }

    @Override
    public void render(float mouseX, float mouseY) {
        final UIFont font = FontManager.getFont("tenacity", 8);

        float widthPercentage = ((numberProperty.getProperty().floatValue()) - numberProperty.getMin()) / (numberProperty.getMax() - numberProperty.getMin());

        final float sliderY = getY() + 10,
                startX = getX() + 10;

        if (dragging) {
            float percent = Math.min(1, Math.max(0, (mouseX - startX) / getWidth()));
            double propertyValue = MathUtil.interpolate(numberProperty.getMin(), numberProperty.getMax(), percent);
            propertyValue = snapToIncrement(propertyValue, numberProperty.getIncrement());
            numberProperty.setProperty(new ImFloat((float) propertyValue));
        }

        SkijaUtil.roundedRectangle(startX, sliderY, getWidth(), 4, 2, new Color(50, 50, 50));

        SkijaUtil.scissored(startX, sliderY, getWidth() * widthPercentage, 4, () -> {
            SkijaUtil.roundedRectangleGradient(startX, sliderY, getWidth(), 4, 2,
                    Astralis.getInstance().getFirstColor(),
                    Astralis.getInstance().getSecondColor(),
                    false
            );
        });

        SkijaUtil.roundedRectangle(
                startX + ((getWidth() - 7) * widthPercentage),
                getY() + 8, 7, 7, 3,
                Astralis.getInstance().getFirstColor()
        );

        final String label = numberProperty.getName() + " " + numberProperty.getProperty().floatValue();

        font.drawString(label,  startX + (getWidth() - font.getStringWidth(label)) / 2f, getY() - 1, Color.WHITE);
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, getX() + 10, getY() + 8, getWidth(), 6) && button == 0) {
            dragging = true;
        }
    }

    @Override
    public void release(double mouseX, double mouseY, int button) {
        if (dragging) dragging = false;
    }

    private double snapToIncrement(double value, float increment) {
        if (increment == 0) return value;
        // we round it again to a decimal place bc it sometimes adds into 0.0000001 idk why so doing this fixes it.
        return MathUtil.roundToDecimalPlaces(Math.round(value / increment) * increment, 6 /* should be alr */);
    }
}
