package cc.astralis.ui.screens.clickgui.dropdown.impl.properties;

import cc.astralis.Astralis;
import cc.astralis.font.UIFont;
import cc.astralis.font.FontManager;
import cc.astralis.module.SubModule;
import cc.astralis.property.properties.ClassModeProperty;
import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import cc.astralis.ui.screens.clickgui.dropdown.Component;
import cc.astralis.util.render.RenderUtil;
import cc.astralis.skija.utils.SkijaUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClassModeComponent extends Component {
    private final Animation expandAnimation = new Animation(Easing.EASE_IN_OUT_QUAD, 250);
    private final ClassModeProperty classModeProperty;
    private boolean expanded, isAnimating, animatingDown;

    private final UIFont font = FontManager.getFont("tenacity", 8);

    public ClassModeComponent(ClassModeProperty classModeProperty) {
        this.classModeProperty = classModeProperty;
        this.setProperty(classModeProperty);
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
        SubModule currentMode = classModeProperty.getProperty();
        String modeName = currentMode != null ? currentMode.getFormatedName() : "None";
        font.drawString(classModeProperty.getName() + ": " + modeName, getX() + 4, getY() + 5.5f, Color.WHITE);

        List<SubModule> modes = new ArrayList<>(classModeProperty.getClassModes().values());
        float totalModesHeight = modes.size() * 16;

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
                    SubModule current = classModeProperty.getProperty();

                    for (SubModule mode : modes) {
                        renderModeOption(mode, mode.equals(current), yOffset, mouseX, mouseY);
                        yOffset += 16;
                    }
                });

                setHeight(18 + (int) animatedHeight);
            }
        } else {
            setHeight(18);
        }
    }

    private void renderModeOption(SubModule mode, boolean isSelected, float yPos, float mouseX, float mouseY) {
        String modeName = mode != null ? mode.getFormatedName() : "None";

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

        font.drawString(modeName, getX() + 14, yPos + 3, isSelected ? Color.WHITE : new Color(200, 200, 200));
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
            List<SubModule> modes = new ArrayList<>(classModeProperty.getClassModes().values());
            float yOffset = getY() + 18;

            for (SubModule mode : modes) {
                if (RenderUtil.isHovered(mouseX, mouseY, getX() + 8, yOffset, getWidth() - 16, 16)) {
                    SubModule currentMode = classModeProperty.getProperty();
                    if (currentMode != null) currentMode.setSelected(false);
                    mode.setSelected(true);
                    classModeProperty.setProperty(mode);
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