package cc.astralis.ui.screens.clickgui.dropdown.impl;

import cc.astralis.Astralis;
import cc.astralis.font.FontManager;
import cc.astralis.font.UIFont;
import cc.astralis.module.Module;
import cc.astralis.property.Property;
import cc.astralis.property.properties.*;
import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import cc.astralis.ui.screens.clickgui.dropdown.impl.properties.*;
import cc.astralis.ui.screens.clickgui.dropdown.Component;
import cc.astralis.util.render.ColorUtil;
import cc.astralis.util.render.RenderUtil;
import cc.astralis.skija.utils.SkijaUtil;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleComponent extends Component {
    public final List<Component> propertiesList = new ArrayList<>();
    private final List<ModuleComponent> moduleComponentList;
    final Animation dropDownAnimation = new Animation(Easing.EASE_IN_OUT_QUAD, 500);

    private boolean animatingDown = true;
    boolean isAnimating = false;

    @Getter
    private final Module module;
    public boolean extended;

    public ModuleComponent(Module module, List<ModuleComponent> moduleComponentList) {
        this.module = module;
        this.moduleComponentList = moduleComponentList;
        this.extended = false;

        for (Property<?> property : module.getPropertyList()) {
            addProperties(property);
        }
    }

    @Override
    public void render(float mouseX, float mouseY) {
        final UIFont font11 = FontManager.getFont("tenacity", 11);

        Color baseColor = ColorUtil.intToColor(0xBB000000);
        final int baseAlpha = 187;

        boolean isLastModule = moduleComponentList.getLast() == this;
        boolean shouldBeRounded = isLastModule && !extended & !isAnimating;

        if (shouldBeRounded) {
            SkijaUtil.drawShaderRectangleVarying(getX(), getY(), getWidth(), getHeight(), 0, 0, 5, 5);
        } else {
            SkijaUtil.drawShaderRectangle(getX(), getY(), getWidth(), getHeight());
        }

        if (!module.isToggled()) {
            if (shouldBeRounded) {
                SkijaUtil.roundedRectangleVarying(getX(), getY(), getWidth(), getHeight(), 0, 0, 5, 5, baseColor);
            } else {
                SkijaUtil.rectangle(getX(), getY(), getWidth(), getHeight(), baseColor);
            }
        } else {
            if (shouldBeRounded) {
                SkijaUtil.roundedRectangleGradientVarying(getX(), getY(), getWidth(), getHeight(),
                        0, 0, 5, 5,
                        ColorUtil.withAlpha(Astralis.getInstance().getFirstColor(), baseAlpha),
                        ColorUtil.withAlpha(Astralis.getInstance().getSecondColor(), baseAlpha),
                        false
                );
            } else {
                SkijaUtil.rectangleGradient(getX(), getY(), getWidth(), getHeight(),
                        ColorUtil.withAlpha(Astralis.getInstance().getFirstColor(), baseAlpha),
                        ColorUtil.withAlpha(Astralis.getInstance().getSecondColor(), baseAlpha),
                        false
                );
            }
        }

        Color textColor = new Color(245, 245, 245);

        final float offset = 5;
        font11.drawString(module.getName(), getX() + offset, getY() + (getHeight() - font11.getStringHeight(module.getName())) / 2, textColor);

        if (extended || isAnimating) {
            final float[] propertyY = {getY() + getHeight()};

            float animatedHeight = (float) dropDownAnimation.getValue();

            if (animatedHeight > 0) {
                SkijaUtil.drawShaderRectangle(getX(), propertyY[0], getWidth(), animatedHeight);
            }
        }

        if (extended || isAnimating) {
            final float[] propertyY = {getY() + getHeight()};
            float totalPropertiesHeight = 2;

            for (Component propertyComponent : propertiesList) {
                if (propertyComponent.getProperty().getVisible().get()) {
                    totalPropertiesHeight += propertyComponent.getHeight() + 2;
                }
            }

            if (animatingDown) {
                dropDownAnimation.setStartPoint(0);
                dropDownAnimation.setEndPoint(totalPropertiesHeight);
                dropDownAnimation.run(totalPropertiesHeight);
            } else {
                dropDownAnimation.setStartPoint(totalPropertiesHeight);
                dropDownAnimation.setEndPoint(0);
                dropDownAnimation.run(0);
            }

            float animatedHeight = (float) dropDownAnimation.getValue();
            float finalTotalPropertiesHeight = totalPropertiesHeight;

            if (!animatingDown && animatedHeight <= 0.1f) {
                isAnimating = false;
                animatedHeight = 0;
            } else if (animatingDown && animatedHeight >= totalPropertiesHeight - 0.1f) {
                animatedHeight = totalPropertiesHeight;
            }

            if (animatedHeight > 0) {
                SkijaUtil.scissored(getX(), propertyY[0], getWidth(), animatedHeight, () -> {
                    SkijaUtil.rectangle(getX(), propertyY[0], getWidth(), finalTotalPropertiesHeight,
                            ColorUtil.intToColor(0xBB000000));

                    for (Component propertyComponent : propertiesList) {
                        if (propertyComponent.getProperty().getVisible().get()) {
                            propertyComponent.setX(getX());
                            propertyComponent.setY(propertyY[0]);
                            propertyComponent.render(mouseX, mouseY);
                            propertyY[0] += propertyComponent.getHeight() + 2;
                        }
                    }
                });
            }
        }
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (extended) {
            propertiesList.forEach(propertyComponent -> propertyComponent.click(mouseX, mouseY, button));
        }

        if (RenderUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            if (button == 0) {
                module.toggle();
            } else if (button == 1 && !propertiesList.isEmpty()) {
                extended = !extended;
                animatingDown = extended;
                isAnimating = true;
                dropDownAnimation.reset();
            }
        }
    }

    @Override
    public void release(double mouseX, double mouseY, int button) {
        propertiesList.forEach(categoryComponent -> categoryComponent.release(mouseX, mouseY, button));
    }

    private void addProperties(Property<?> property) {
        switch (property) {
            case BooleanProperty booleanProperty -> propertiesList.add(new BooleanComponent(booleanProperty));
            case ModeProperty modeProperty -> propertiesList.add(new ModeComponent(modeProperty));
            case NumberProperty numberProperty -> propertiesList.add(new SliderComponent(numberProperty));
            case ColorProperty colorProperty -> propertiesList.add(new ColorPickerComponent(colorProperty));
            case ClassModeProperty classModeProperty -> propertiesList.add(new ClassModeComponent(classModeProperty));
            default ->
                /* we could through an exception but since there are multiple properties I didn't do yet, so I will add it later */
                    Astralis.LOGGER.error("Unknown Property! " + property.getName());
        }
    }
}