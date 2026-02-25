package cc.astralis.ui.screens.clickgui.dropdown.impl.properties;

import cc.astralis.Astralis;
import cc.astralis.font.UIFont;
import cc.astralis.font.FontManager;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import cc.astralis.ui.screens.clickgui.dropdown.Component;
import cc.astralis.util.render.RenderUtil;
import cc.astralis.skija.utils.SkijaUtil;

import java.awt.*;

public class ModeComponent extends Component {
    private final Animation expandAnimation = new Animation(Easing.EASE_IN_OUT_QUAD, 250);
    private final ModeProperty modeProperty;
    private boolean expanded, isAnimating, animatingDown;

    private final UIFont font = FontManager.getFont("tenacity", 8);

    public ModeComponent(ModeProperty modeProperty) {
        this.modeProperty = modeProperty;
        this.setProperty(modeProperty);
        this.setWidth(130);
        this.setHeight(18);
        this.expanded = false;
        this.isAnimating = false;
        this.animatingDown = true;

        this.expandAnimation.setStartPoint(0);
        this.expandAnimation.setEndPoint(1);
    }

    @Override
    public void render(float mouseX, float mouseY) {
        font.drawString(modeProperty.getName() + ": " + modeProperty.getProperty(), getX() + 4, getY() + 5.5f, Color.WHITE);
        /*FontManager.getFont("Icons-Regular", 10)
                .drawString(expanded ? "V" : "W", getX() + getWidth() - 20, getY() + 5.5f, new Color(150, 150, 150));*/

        String[] modes = modeProperty.getModes();
        float totalModesHeight = modes.length * 16;

        if (expanded || isAnimating) {
            if (animatingDown) {
                expandAnimation.setStartPoint(0);
                expandAnimation.setEndPoint(totalModesHeight);
                expandAnimation.run(totalModesHeight);
            } else {
                expandAnimation.setStartPoint(totalModesHeight);
                expandAnimation.setEndPoint(0);
                expandAnimation.run(0);
            }

            float animatedHeight = (float) expandAnimation.getValue();

            if (!animatingDown && animatedHeight <= 0.1f) {
                isAnimating = false;
                animatedHeight = 0;
                setHeight(18);
            } else if (animatingDown && animatedHeight >= totalModesHeight - 0.1f) {
                animatedHeight = totalModesHeight;
                setHeight(18 + (int) totalModesHeight);
            }

            if (animatedHeight > 0) {
                float modesY = getY() + 18;

                SkijaUtil.scissored(getX(), modesY, getWidth(), animatedHeight, () -> {
                    float yOffset = modesY;
                    String currentMode = modeProperty.getProperty();

                    for (String mode : modes) {
                        renderModeOption(mode, mode.equals(currentMode), yOffset, mouseX, mouseY);
                        yOffset += 16;
                    }
                });

                setHeight(18 + (int) animatedHeight);
            }
        } else {
            setHeight(18);
        }
    }

    private void renderModeOption(String mode, boolean isSelected, float yPos, float mouseX, float mouseY) {
        if (isSelected) {
            SkijaUtil.roundedRectangleGradient(
                    getX() + 8, yPos + 1, getWidth() - 16, 14, 3,
                    Astralis.getInstance().getFirstColor(),
                    Astralis.getInstance().getSecondColor(),
                    false
            );
        } else if (RenderUtil.isHovered(mouseX, mouseY, getX() + 8, yPos, getWidth() - 16, 16)) {
            SkijaUtil.roundedRectangle(getX() + 8, yPos + 1, getWidth() - 16, 14, 3,
                    new Color(45, 45, 45, 120));
        }

        font.drawString(mode, getX() + 14, yPos + 3, isSelected ? Color.WHITE : new Color(200, 200, 200));
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), 18)) {
            expanded = !expanded;
            animatingDown = expanded;
            isAnimating = true;
            expandAnimation.reset();
            return;
        }

        if (expanded) {
            String[] modes = modeProperty.getModes();
            float yOffset = getY() + 18;

            for (String mode : modes) {
                if (RenderUtil.isHovered(mouseX, mouseY, getX() + 8, yOffset, getWidth() - 16, 16)) {
                    modeProperty.setProperty(mode);
                    expanded = false;
                    animatingDown = false;
                    isAnimating = true;
                    expandAnimation.reset();
                    return;
                }

                yOffset += 16;
            }
        }
    }
}