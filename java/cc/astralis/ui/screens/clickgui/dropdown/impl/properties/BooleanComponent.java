package cc.astralis.ui.screens.clickgui.dropdown.impl.properties;

import cc.astralis.Astralis;
import cc.astralis.font.FontManager;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import cc.astralis.ui.screens.clickgui.dropdown.Component;
import cc.astralis.util.render.RenderUtil;
import cc.astralis.skija.utils.SkijaUtil;

import java.awt.*;

public class BooleanComponent extends Component {
    private final BooleanProperty booleanProperty;
    private final Animation switchAnimation = new Animation(Easing.EASE_OUT_BACK, 200);

    public BooleanComponent(BooleanProperty booleanProperty) {
        this.booleanProperty = booleanProperty;
        this.setProperty(booleanProperty);
        this.setWidth(130);
        this.setHeight(15);

        this.switchAnimation.setStartPoint(0);
        this.switchAnimation.setEndPoint(1);
    }

    @Override
    public void render(float mouseX, float mouseY) {
        FontManager.getFont("tenacity", 8).drawString(
                booleanProperty.getName(),
                getX() + 4,
                getY() + 4,
                booleanProperty.getProperty() ? Color.WHITE : new Color(180, 180, 180)
        );

        switchAnimation.run(booleanProperty.getProperty() ? 1f : 0f);

        float animatedPos = (float) switchAnimation.getValue();

        final float switchX = getX() + getWidth() - 30,
                switchY = getY() + 4f;

        final float switchWidth = 24,
                switchHeight = 10;

        if (booleanProperty.getProperty()) {
            SkijaUtil.roundedRectangleGradient(
                    switchX, switchY, switchWidth, switchHeight, 5,
                    Astralis.getInstance().getFirstColor(),
                    Astralis.getInstance().getSecondColor(),
                    false
            );
        } else {
            SkijaUtil.roundedRectangle(
                    switchX, switchY, switchWidth, switchHeight, 5,
                    new Color(50, 50, 50)
            );
        }

        final float knobX = switchX + 1 + (animatedPos * (switchWidth - 10)),
                knobY = switchY + 1;

        SkijaUtil.roundedRectangle(
                knobX + 0.5f, knobY + 0.5f, 8, 8, 4,
                new Color(0, 0, 0, 50)
        );

        SkijaUtil.roundedRectangle(
                knobX, knobY, 8, 8, 4,
                Color.WHITE
        );
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            booleanProperty.setProperty(!booleanProperty.getProperty());
        }
    }
}